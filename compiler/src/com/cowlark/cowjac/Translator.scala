package com.cowlark.cowjac
import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.JavaConversions.collectionAsScalaIterable
import scala.collection.immutable.HashMap

import com.cowlark.cowjac.DependencyAnalyser

import soot.jimple.toolkits.annotation.tags.NullCheckTag
import soot.jimple.AbstractJimpleValueSwitch
import soot.jimple.AbstractStmtSwitch
import soot.jimple.AddExpr
import soot.jimple.AndExpr
import soot.jimple.ArrayRef
import soot.jimple.AssignStmt
import soot.jimple.BinopExpr
import soot.jimple.CastExpr
import soot.jimple.CaughtExceptionRef
import soot.jimple.ClassConstant
import soot.jimple.CmpExpr
import soot.jimple.CmpgExpr
import soot.jimple.CmplExpr
import soot.jimple.DefinitionStmt
import soot.jimple.DivExpr
import soot.jimple.DoubleConstant
import soot.jimple.EnterMonitorStmt
import soot.jimple.EqExpr
import soot.jimple.ExitMonitorStmt
import soot.jimple.FieldRef
import soot.jimple.FloatConstant
import soot.jimple.GeExpr
import soot.jimple.GotoStmt
import soot.jimple.GtExpr
import soot.jimple.IdentityStmt
import soot.jimple.IfStmt
import soot.jimple.InstanceFieldRef
import soot.jimple.InstanceInvokeExpr
import soot.jimple.InstanceOfExpr
import soot.jimple.IntConstant
import soot.jimple.InterfaceInvokeExpr
import soot.jimple.InvokeExpr
import soot.jimple.InvokeStmt
import soot.jimple.LeExpr
import soot.jimple.LengthExpr
import soot.jimple.LongConstant
import soot.jimple.LtExpr
import soot.jimple.MulExpr
import soot.jimple.NeExpr
import soot.jimple.NegExpr
import soot.jimple.NewArrayExpr
import soot.jimple.NewExpr
import soot.jimple.NullConstant
import soot.jimple.OrExpr
import soot.jimple.ParameterRef
import soot.jimple.RemExpr
import soot.jimple.ReturnStmt
import soot.jimple.ReturnVoidStmt
import soot.jimple.ShlExpr
import soot.jimple.ShrExpr
import soot.jimple.SpecialInvokeExpr
import soot.jimple.StaticFieldRef
import soot.jimple.StaticInvokeExpr
import soot.jimple.StringConstant
import soot.jimple.SubExpr
import soot.jimple.ThisRef
import soot.jimple.ThrowStmt
import soot.jimple.UnopExpr
import soot.jimple.UshrExpr
import soot.jimple.VirtualInvokeExpr
import soot.jimple.XorExpr
import soot.tagkit.AnnotationStringElem
import soot.tagkit.VisibilityAnnotationTag
import soot.toolkits.graph.BriefUnitGraph
import soot.ArrayType
import soot.BooleanType
import soot.ByteType
import soot.CharType
import soot.ClassMember
import soot.DoubleType
import soot.FloatType
import soot.IntType
import soot.Local
import soot.LongType
import soot.PrimType
import soot.RefLikeType
import soot.RefType
import soot.ShortType
import soot.SootClass
import soot.SootField
import soot.SootFieldRef
import soot.SootMethod
import soot.SootMethodRef
import soot.Type
import soot.TypeSwitch
import soot.VoidType

object Translator extends DependencyAnalyser
{
	private var namecache = HashMap[String, String]()
	
	private val splitpoints = Array('.', '/')
	private def reformName(jname: String, separator: String): String =
		jname.split(splitpoints).reduceLeft(_ + separator + _)
		
	private val javaToCXX = Memoize((s: String) => "::" + reformName(s, "::")) 
	
	private def className(s: String) =
		javaToCXX(s)
		
	private def className(c: SootClass): String =
		className(c.getName)
		
	private def fieldName(f: SootFieldRef) =
		"f_" + f.name
		
	private def fieldName(f: SootField) =
		"f_" + f.getName

	private def methodNameImpl(m: SootMethod): String =
	{
		for (tag <- m.getTags if tag.getName == "VisibilityAnnotationTag")
		{
			val vat = tag.asInstanceOf[VisibilityAnnotationTag]
			for (a <- vat.getAnnotations if a.getType == "Lcom/cowlark/cowjac/harmony/Native;")
			{
				val s = a.getElemAt(0).asInstanceOf[AnnotationStringElem]
				return s.getValue
			}
		}
			
		def hex2(i: Integer) =
			(if (i < 16) "0" else "") + Integer.toHexString(i)
			
		val sb = new StringBuilder("m_")
		for (c <- m.getBytecodeSignature)
		{
			if (c.isLetterOrDigit)
				sb += c
			else
			{
				sb += '_'
				sb ++= hex2(c.toInt)
			}
		}
		
		return sb.toString
	}
	
	private val methodName = Memoize(methodNameImpl)
	
	private def methodName(m: SootMethodRef): String =
		methodName(m.resolve)
	
	private def translateModifier(cm: ClassMember, p: Printer)
	{
		if (cm.isPrivate)
			p.print("private: ")
		else if (cm.isProtected)
			p.print("protected: ")
		else
			p.print("public: ")
			
		if (cm.isStatic)
			p.print("static ")			
	}
	
	private def translateType(t: Type, p: Printer)
	{
		object TS extends TypeSwitch
		{
			override def caseVoidType(t: VoidType) = p.print("void")
			override def caseBooleanType(t: BooleanType) = p.print("jboolean")
			override def caseByteType(t: ByteType) = p.print("jbyte")
			override def caseCharType(t: CharType) = p.print("jchar")
			override def caseShortType(t: ShortType) = p.print("jshort")
			override def caseIntType(t: IntType) = p.print("jint")
			override def caseLongType(t: LongType) = p.print("jlong")
			override def caseFloatType(t: FloatType) = p.print("jfloat")
			override def caseDoubleType(t: DoubleType) = p.print("jdouble")
			
			override def caseArrayType(t: ArrayType)
			{
				p.print("::com::cowlark::cowjac::Array< ")
				t.getElementType.apply(TS)
				p.print(" >*")
			}
			
			override def caseRefType(t: RefType)
			{
				p.print(className(t.getSootClass), "*")
			}
			
			override def defaultCase(t: Type) = assert(false)
		}
		t.apply(TS)
	}
	
	def translate(sootclass: SootClass, ps: PrintSet)
	{
		var stringconstants = Map.empty[String, Integer]
		
		def translateFieldDeclaration(field: SootField)
		{
			val isref = field.getType.isInstanceOf[RefLikeType]
	
			ps.h.print("\t")
			translateModifier(field, ps.h)
			if (isref && field.isStatic)
				ps.h.print("::com::cowlark::cowjac::GlobalReference< ")
			translateType(field.getType, ps.h)
			if (isref && field.isStatic)
				ps.h.print(" >")
			ps.h.print(" (", fieldName(field), ");\n")
		}
		
		def translateFieldDefinition(field: SootField)
		{
			if (field.isStatic)
			{
				val isref = field.getType.isInstanceOf[RefLikeType]
				
				if (isref)
					ps.ch.print("::com::cowlark::cowjac::GlobalReference< ")
				translateType(field.getType, ps.ch)
				if (isref)
					ps.ch.print(" >")
					
				ps.ch.print(" (", className(field.getDeclaringClass), "::",
						fieldName(field), ");\n")
			}
		}
		
		def translateMethodDeclaration(method: SootMethod)
		{
			ps.h.print("\t")
			translateModifier(method, ps.h)
			
			if (!method.isPrivate && !method.isStatic)
				ps.h.print("virtual ")
				
			translateType(method.getReturnType, ps.h)
			ps.h.print(" ", methodName(method))
	
			ps.h.print("(com::cowlark::cowjac::Stackframe*")
			
			for (to <- method.getParameterTypes)
			{
				val t = to.asInstanceOf[Type]
	
				ps.h.print(", ")
				translateType(t, ps.h)
			}
				
			ps.h.print(")")
			
			if (method.isAbstract)
				ps.h.print(" = 0")
			ps.h.print(";\n")
		}
		
		def translateMethodDefinition(method: SootMethod)
		{
			val body = method.getActiveBody
			
			translateType(method.getReturnType, ps.c)
			ps.c.print(" ", className(method.getDeclaringClass), "::",
					methodName(method),
					"(com::cowlark::cowjac::Stackframe* parentFrame")
			
			for (i <- 0 until method.getParameterCount)
			{
				val t = method.getParameterType(i)
				
				ps.c.print(", ")
				translateType(t, ps.c)
				ps.c.print(" p", String.valueOf(i))
			}
			
			ps.c.print(")\n{\n")
			
			/* Declare stackframe structure. */
	
			ps.c.print("\tstruct frame : public com::cowlark::cowjac::Stackframe\n")
			ps.c.print("\t{\n");
			ps.c.print("\t\tframe(com::cowlark::cowjac::Stackframe* p):\n")
			ps.c.print("\t\t\tcom::cowlark::cowjac::Stackframe(p)\n")
		
			val reflike = body.getLocals.filter(s => s.getType.isInstanceOf[RefLikeType])
			
			ps.c.print("\t\t{\n")
			if (!reflike.isEmpty)
			{
				ps.c.print("\t\t\tmemset(&f",
						reflike.first.getName,
						", 0, sizeof(f",
						reflike.first.getName,
						") * ",
						String.valueOf(reflike.size),
						");\n")
			}
			ps.c.print("\t\t}\n")
			
			ps.c.print("\n")
			
			if (!reflike.isEmpty)
			{
				ps.c.print("\t\tvoid mark()\n")
				ps.c.print("\t\t{\n")
				
				ps.c.print("\t\t\tmarkMany(&f",
						reflike.first.getName, ", ",
						String.valueOf(reflike.size),
						");\n")
				
				ps.c.print("\t\t}\n")
			}
			
			ps.c.print("\n")
			ps.c.print("public:\n")
			
			for (local <- reflike)
			{
				val t = local.getType
	
				ps.c.print("\t\t::com::cowlark::cowjac::ContainsReferences* ",
						"f", local.getName, ";\n")
			}
	
			ps.c.print("\t};\n");
			ps.c.print("\tframe F(parentFrame);\n")
			ps.c.print("\t::com::cowlark::cowjac::Object* caughtexception;\n")
			ps.c.print("\n")
			
			/* Declare locals that don't need to go in the frame. */
			
			for (local <- body.getLocals)
			{
				val t = local.getType
				ps.c.print("\t")
				translateType(t, ps.c)
				ps.c.print(" j", local.getName, " = 0;\n")
			}
			
			/* The method body itself. */
			
			var labels = HashMap.empty[soot.Unit, Integer]
			val ug = new BriefUnitGraph(body)
			var notnull = false
			
			def label(unit: soot.Unit): String =
			{
				val s = labels.get(unit)
				if (s != None)
					return "L" + s.get
	
				val i = labels.size
				labels += (unit -> i)
				
				return "L" + i
			}
	
			object NS extends TypeSwitch
			{
				override def caseRefType(t: RefType) =
					ps.c.print(javaToCXX(t.getSootClass.getName))
				
				override def defaultCase(t: Type) = assert(false)
			}
	
			object VS extends AbstractJimpleValueSwitch
			{
				override def caseIntConstant(s: IntConstant) =
					ps.c.print("(jint)0x", s.value.toHexString)
				
				override def caseLongConstant(s: LongConstant) =
					ps.c.print("(jlong)0x", s.value.toHexString, "LL")
				
				override def caseFloatConstant(s: FloatConstant) =
					ps.c.print(s.value.toString, "fLL")
				
				override def caseDoubleConstant(s: DoubleConstant) =
					ps.c.print(s.value.toString)
				
				override def caseStringConstant(s: StringConstant) =
				{
					val cido = stringconstants.get(s.value)
					val cid =
						if (cido == None)
						{
							val n = stringconstants.size
							stringconstants += (s.value -> n)
							
							ps.ch.print("static const jchar scd",
									String.valueOf(n), "[] = {")
									
							var first = true
							for (c <- s.value)
							{
								if (!first)
									ps.ch.print(", ")
								else
									first = false
									
								ps.ch.print(String.valueOf(c.toInt))
							}
							
							ps.ch.print("};\n")
							ps.ch.print("static ::com::cowlark::cowjac::GlobalReference< ::java::lang::String* > sc",
									String.valueOf(n), ";\n")
							
							n
						}
						else
							cido.get
							
					ps.c.print("sc", String.valueOf(cid))
				}
				
				override def caseNullConstant(s: NullConstant) =
					ps.c.print("0")
					
				override def caseClassConstant(s: ClassConstant) =
					ps.c.print(className(s.value), "::classInit(&F)->CLASS");
					
				override def caseThisRef(v: ThisRef) =
					ps.c.print("this")
					
				override def caseLocal(v: Local) =
					ps.c.print("j", v.getName)
				
				override def caseInstanceFieldRef(v: InstanceFieldRef) =
				{
					v.getBase.apply(VS)
					ps.c.print("->", className(v.getFieldRef.declaringClass),
							"::", fieldName(v.getFieldRef))
				}
				
				override def caseStaticFieldRef(v: StaticFieldRef) =
					ps.c.print(className(v.getFieldRef.declaringClass), "::classInit(&F)->",
							fieldName(v.getFieldRef))
				
				override def caseArrayRef(v: ArrayRef) =
				{
					if (!notnull)
						ps.c.print("::com::cowlark::cowjac::NullCheck(")
					v.getBase.apply(VS)
					if (!notnull)
						ps.c.print(")")
					ps.c.print("->ref(&F, ")
					v.getIndex.apply(VS)
					ps.c.print(")")
				}
				
				override def caseLengthExpr(v: LengthExpr) =
				{
					if (!notnull)
						ps.c.print("::com::cowlark::cowjac::NullCheck(")
					v.getOp.apply(VS)
					if (!notnull)
						ps.c.print(")")
					ps.c.print("->length()")
				}
					
				override def caseParameterRef(v: ParameterRef) =
					ps.c.print("p", String.valueOf(v.getIndex))
				
				override def caseCastExpr(v: CastExpr) =
				{
					if (v.getCastType.isInstanceOf[PrimType])
					{
						ps.c.print("(")
						translateType(v.getCastType, ps.c)
						ps.c.print(")(")
						v.getOp.apply(VS)
						ps.c.print(")")
					}
					else
					{
						ps.c.print("::com::cowlark::cowjac::Cast< ")
						translateType(v.getOp.getType, ps.c)
						ps.c.print(", ")
						translateType(v.getCastType, ps.c)
						ps.c.print(" >(&F, ")
						v.getOp.apply(VS)
						ps.c.print(")")
					}
				}
				
				override def caseInstanceOfExpr(v: InstanceOfExpr) =
				{
					/* The result of this is a boolean; the pointer we return
					 * will get implicitly cast to the right type, so we don't
					 * need to do it explicitly. */
					ps.c.print("dynamic_cast< ")
					translateType(v.getCheckType, ps.c)
					ps.c.print(" >(")
					v.getOp.apply(VS)
					ps.c.print(")")
				}
				
				override def caseAddExpr(v: AddExpr) = caseBinopExpr(v)
				override def caseSubExpr(v: SubExpr) = caseBinopExpr(v)
				override def caseMulExpr(v: MulExpr) = caseBinopExpr(v)
				override def caseDivExpr(v: DivExpr) = caseBinopExpr(v)
				override def caseRemExpr(v: RemExpr) = caseBinopExpr(v)
				override def caseShlExpr(v: ShlExpr) = caseBinopExpr(v)
				override def caseShrExpr(v: ShrExpr) = caseBinopExpr(v)
				override def caseUshrExpr(v: UshrExpr) = caseBinopXExpr(v, "Ushr")
				override def caseGeExpr(v: GeExpr) = caseBinopExpr(v)
				override def caseGtExpr(v: GtExpr) = caseBinopExpr(v)
				override def caseLeExpr(v: LeExpr) = caseBinopExpr(v)
				override def caseLtExpr(v: LtExpr) = caseBinopExpr(v)
				override def caseEqExpr(v: EqExpr) = caseBinopExpr(v)
				override def caseNeExpr(v: NeExpr) = caseBinopExpr(v)
				override def caseCmpExpr(v: CmpExpr) = caseBinopXExpr(v, "Cmp")
				override def caseCmpgExpr(v: CmpgExpr) = caseBinopXExpr(v, "Cmpg")
				override def caseCmplExpr(v: CmplExpr) = caseBinopXExpr(v, "Cmpl")
				override def caseAndExpr(v: AndExpr) = caseBinopExpr(v)
				override def caseOrExpr(v: OrExpr) = caseBinopExpr(v)
				override def caseXorExpr(v: XorExpr) = caseBinopExpr(v)
				
				private def caseBinopExpr(v: BinopExpr) =
				{
					v.getOp1.apply(VS)
					ps.c.print(v.getSymbol)
					v.getOp2.apply(VS)
				}
	
				private def caseBinopXExpr(v: BinopExpr, x: String) =
				{
					ps.c.print("::com::cowlark::cowjac::", x, "(")
					v.getOp1.apply(VS)
					ps.c.print(", ")
					v.getOp2.apply(VS)
					ps.c.print(")")
				}
				
				override def caseNegExpr(v: NegExpr) =
				{
					ps.c.print("-")
					v.getOp.apply(VS)
				}
				
				override def caseNewExpr(v: NewExpr) =
				{
					ps.c.print("new ")
					v.getType.apply(NS)
				}
				
				override def caseNewArrayExpr(v: NewArrayExpr) =
				{
					ps.c.print("new ::com::cowlark::cowjac::Array< ")
					translateType(v.getBaseType, ps.c)
					ps.c.print(" >(&F, ")
					v.getSize.apply(VS)
					ps.c.print(")")
				}
				
				private def parameters(v: InvokeExpr)
				{
					ps.c.print("(&F")
					
					for (arg <- v.getArgs)
					{
						ps.c.print(", ")
						arg.apply(VS)
					}
					
					ps.c.print(")")
				}
				
				override def caseInterfaceInvokeExpr(v: InterfaceInvokeExpr) =
					caseInstanceInvokeExpr(v)
					
				override def caseVirtualInvokeExpr(v: VirtualInvokeExpr) =
					caseInstanceInvokeExpr(v)
					
				def caseInstanceInvokeExpr(v: InstanceInvokeExpr) =
				{
					if (!notnull)
						ps.c.print("com::cowlark::cowjac::NullCheck(")
					v.getBase.apply(VS)
					if (!notnull)
						ps.c.print(")")
						
					ps.c.print("->", methodName(v.getMethodRef))
					parameters(v)
				}
					
				override def caseSpecialInvokeExpr(v: SpecialInvokeExpr) =
				{
					if (!notnull)
						ps.c.print("com::cowlark::cowjac::NullCheck(")
					v.getBase.apply(VS)
					if (!notnull)
						ps.c.print(")")
						
					ps.c.print("->", className(v.getMethodRef.declaringClass),
							"::", methodName(v.getMethodRef))
						
					parameters(v)
				}
				
				override def caseStaticInvokeExpr(v: StaticInvokeExpr) =
				{
					ps.c.print(className(v.getMethodRef.declaringClass), "::",
							methodName(v.getMethodRef))
					
					parameters(v)
				}
				
				override def defaultCase(s: Any) = assert(false)
			}
			
			object SS extends AbstractStmtSwitch
			{
				override def caseIdentityStmt(s: IdentityStmt) = caseDefinitionStmt(s)
				override def caseAssignStmt(s: AssignStmt) = caseDefinitionStmt(s)
				
				override def caseReturnStmt(s: ReturnStmt) =
				{
					ps.c.print("\treturn ")
					s.getOp.apply(VS)
					ps.c.print(";\n")
				}
				
				override def caseReturnVoidStmt(s: ReturnVoidStmt) =
					ps.c.print("\treturn;\n")
				
				override def caseIfStmt(s: IfStmt) =
				{
					ps.c.print("\tif (")
					s.getCondition.apply(VS)
					ps.c.print(") goto ", label(s.getTarget), ";\n")
				}
					
				override def caseInvokeStmt(s: InvokeStmt) =
				{
					ps.c.print("\t")
					s.getInvokeExpr.apply(VS)
					ps.c.print(";\n")
				}
					
				def caseDefinitionStmt(s: DefinitionStmt) =
				{
					ps.c.print("\t")
					if (s.getLeftOp.isInstanceOf[Local] &&
							s.getLeftOp.getType.isInstanceOf[RefLikeType])
					{
						/* Assign to local with is a reference; must remember
						 * to update the stack frame to make GC work. */
						val local = s.getLeftOp.asInstanceOf[Local]
						ps.c.print("F.f", local.getName, " = ")
					}
					s.getLeftOp.apply(VS)
					ps.c.print(" = ")
					
					if (s.getRightOp.isInstanceOf[CaughtExceptionRef])
					{
						ps.c.print("static_cast< ")
						translateType(s.getLeftOp.getType, ps.c)
						ps.c.print(" >(caughtexception)")
					}
					else
						s.getRightOp.apply(VS)
						
					ps.c.print(";\n")
				}
				
				override def caseThrowStmt(s: ThrowStmt) =
				{
					ps.c.print("\tthrow ")
					s.getOp.apply(VS)
					ps.c.print(";\n")
				}
				
				override def caseGotoStmt(s: GotoStmt) =
					ps.c.print("\tgoto ", label(s.getTarget), ";\n")
				
				override def caseEnterMonitorStmt(s: EnterMonitorStmt) =
				{
					ps.c.print("\t")
						
					if (!notnull)
						ps.c.print("com::cowlark::cowjac::NullCheck(")
					s.getOp.apply(VS)
					if (!notnull)
						ps.c.print(")")
						
					ps.c.print("->enterMonitor();\n")
				}
				
				override def caseExitMonitorStmt(s: ExitMonitorStmt) =
				{
					ps.c.print("\t")
						
					if (!notnull)
						ps.c.print("com::cowlark::cowjac::NullCheck(")
					s.getOp.apply(VS)
					if (!notnull)
						ps.c.print(")")
						
					ps.c.print("->leaveMonitor();\n")
				}
				
				override def defaultCase(s: Any) = assert(false)
			}
			
			var oldunit: soot.Unit = null
			for (unit <- body.getUnits)
			{
				/* If this is a target of a jump, then we need to add a label.
				 * An instruction is not a jump target if the only way to it is
				 * from the preceding instruciton. */
				
				val junction = 
					if ((ug.getPredsOf(unit).size == 1) && (ug.getPredsOf(unit).get(0) == oldunit))
						false
					else
						true
					
				if (junction)
					ps.c.print(label(unit), ":\n")
	
				val tag = unit.getTag("NullCheckTag").asInstanceOf[NullCheckTag]
				notnull = (tag != null) && !tag.needCheck()
				unit.apply(SS)
						
				oldunit = unit
			}
			
			ps.c.print("}\n\n")
		}
		
		def forwardDeclare(sootclass: SootClass)
		{
			val nslevels = sootclass.getName.split('.')
			for (i <- 0 to nslevels.length-2)
				ps.h.print("namespace ", nslevels(i), " { ")
			
			ps.h.print("class ", sootclass.getJavaStyleName, "; ")
			
			for (i <- 0 to nslevels.length-2)
				ps.h.print("}")
			ps.h.print("\n")
		}
		

		val jname = sootclass.getName()
		val cxxname = javaToCXX(jname)
		val headername = reformName(jname, "_").toUpperCase() + "_H"
		
		ps.h.print("#ifndef ", headername, "\n")
		ps.h.print("#define ", headername, "\n")
		
		ps.ch.print("#include \"cowjac.h\"\n")
		ps.ch.print("#include \"cowjacarray.h\"\n")
		
		ps.h.print("\n")
		val dependencies = getDependencies(sootclass)
		for (d <- dependencies)
		{
			forwardDeclare(d)
			ps.ch.print("#include \"", d.getName, ".h\"\n")
		}
		
		ps.h.print("\n")
		ps.ch.print("\n")

		var superclasses = Vector.empty[SootClass]
		if (jname != "java.lang.Object")
			superclasses = superclasses :+ sootclass.getSuperclass
		for (s <- superclasses)
			ps.h.print("#include \"", s.getName, ".h\"\n")
		
		val nslevels = jname.split('.')
		for (i <- 0 to nslevels.length-2)
			ps.h.print("namespace ", nslevels(i), " {\n")
		
		ps.h.print("\n")
		
		ps.h.print("class ", sootclass.getJavaStyleName)
		if (!superclasses.isEmpty)
		{
			val superclassnames = superclasses.map((c: SootClass) => "public " + javaToCXX(c.getName))
			ps.h.print(" : ", superclassnames.reduceLeft(_ + ", " + _))
		}
		else
			ps.h.print(" : public com::cowlark::cowjac::Object")
		
		ps.h.print("\n{\n")
		
		ps.h.print("\t/* Class management */\n")
		ps.ch.print("/* Class management */\n")
		
		ps.h.print("\tprivate: static bool initialised;\n")
		ps.ch.print("bool ", className(sootclass), "::initialised = false;\n")
		
		ps.h.print("\tpublic: static ::java::lang::Class* CLASS;\n")
		ps.h.print("\tpublic: static ", className(sootclass),
				"* classInit(com::cowlark::cowjac::Stackframe*);\n")
		ps.h.print("\n")
		
		ps.h.print("\t/* Field declarations */\n")
		ps.ch.print("\n/* Field definitions */\n")
		for (f <- sootclass.getFields)
		{
			translateFieldDeclaration(f)
			translateFieldDefinition(f)
		}
		
		ps.h.print("\n")
		
		ps.h.print("\t/* Method declarations */\n")
		ps.c.print("\n/* Method definitions */\n")
		
		/* Emit constructor and destructor */
		
		ps.h.print("\tpublic: ", sootclass.getShortName, "();\n")
		ps.h.print("\tpublic: virtual ~", sootclass.getShortName, "() {};\n")
				
		/* Ordinary methods */
				
		for (m <- sootclass.getMethods)
		{
			translateMethodDeclaration(m)
			if (m.hasActiveBody)
				translateMethodDefinition(m)
		}
		
		ps.h.print("};\n")
		ps.h.print("\n")
		
		for (i <- 0 to nslevels.length-2)
			ps.h.print("} /* namespace ", nslevels(i), " */\n")
		
		ps.h.print("#endif\n")
	}
}