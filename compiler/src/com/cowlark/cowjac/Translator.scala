package com.cowlark.cowjac
import java.io.PrintStream
import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.JavaConversions.collectionAsScalaIterable
import scala.collection.immutable.HashMap
import com.cowlark.cowjac.DependencyAnalyser
import soot.jimple.AbstractJimpleValueSwitch
import soot.jimple.AbstractStmtSwitch
import soot.jimple.AddExpr
import soot.jimple.AssignStmt
import soot.jimple.BinopExpr
import soot.jimple.DefinitionStmt
import soot.jimple.GeExpr
import soot.jimple.IdentityStmt
import soot.jimple.IfStmt
import soot.jimple.IntConstant
import soot.jimple.InvokeExpr
import soot.jimple.InvokeStmt
import soot.jimple.NewExpr
import soot.jimple.NewArrayExpr
import soot.jimple.ParameterRef
import soot.jimple.ReturnStmt
import soot.jimple.ReturnVoidStmt
import soot.jimple.SpecialInvokeExpr
import soot.jimple.StringConstant
import soot.jimple.ThisRef
import soot.jimple.VirtualInvokeExpr
import soot.jimple.NullConstant
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
import soot.RefLikeType
import soot.RefType
import soot.ShortType
import soot.SootClass
import soot.SootField
import soot.SootMethod
import soot.Type
import soot.TypeSwitch
import soot.VoidType
import soot.jimple.toolkits.annotation.tags.NullCheckTag
import soot.jimple.InstanceFieldRef
import soot.jimple.StaticInvokeExpr
import soot.jimple.ArrayRef
import soot.jimple.LengthExpr
import soot.jimple.ThrowStmt
import soot.jimple.SubExpr
import soot.jimple.MulExpr
import soot.jimple.DivExpr
import soot.jimple.RemExpr
import soot.jimple.ShrExpr
import soot.jimple.ShlExpr
import soot.jimple.LtExpr
import soot.jimple.GtExpr
import soot.jimple.EqExpr
import soot.jimple.LeExpr
import soot.jimple.NeExpr
import soot.jimple.XorExpr
import soot.jimple.AndExpr
import soot.jimple.OrExpr
import soot.jimple.CastExpr
import soot.jimple.GotoStmt
import soot.jimple.InstanceOfExpr
import soot.jimple.CaughtExceptionRef
import soot.jimple.InstanceInvokeExpr
import soot.jimple.InterfaceInvokeExpr
import soot.jimple.StaticFieldRef
import soot.jimple.EnterMonitorStmt
import soot.jimple.ExitMonitorStmt
import soot.jimple.UnopExpr
import soot.jimple.NegExpr
import soot.jimple.CmpExpr
import soot.jimple.CmpgExpr
import soot.jimple.LongConstant
import soot.jimple.UshrExpr
import soot.jimple.CmplExpr
import soot.jimple.DoubleConstant
import soot.jimple.FloatConstant
import soot.jimple.ClassConstant
import soot.jimple.FieldRef
import soot.SootFieldRef
import soot.SootMethodRef

object Translator extends DependencyAnalyser
{
	private var namecache = HashMap[String, String]()
	
	private def reformName(jname: String, separator: String): String =
		jname.split(Array('.', '/')).reduceLeft(_ + separator + _)
		
	private def javaToCXX(jname: String): String =
	{
		val n = namecache.get(jname)
		if (n != None)
			return n.get
		
		val cxxname = "::" + reformName(jname, "::")
		namecache = namecache + (jname -> cxxname)
		return cxxname
	}
	
	private def className(s: String) =
		javaToCXX(s)
		
	private def className(c: SootClass): String =
		className(c.getName)
		
	private def fieldName(f: SootFieldRef) =
		"f_" + f.name
		
	private def fieldName(f: SootField) =
		"f_" + f.getName
	
	private def methodName(s: String) =	
	{
		if (s == "<init>")
			"init"
		else if (s == "<clinit>")
			"clinit"
		else
			"m_" + s
	}
	
	private def methodName(m: SootMethodRef): String =
		methodName(m.name)
	
	private def methodName(m: SootMethod): String =
		methodName(m.getName)
	
	private def translateModifier(cm: ClassMember, ps: PrintStream)
	{
		if (cm.isPrivate)
			ps.print("private: ")
		else if (cm.isProtected)
			ps.print("protected: ")
		else
			ps.print("public: ")
			
		if (cm.isStatic)
			ps.print("static ")			
	}
	
	private def translateType(t: Type, ps: PrintStream)
	{
		object TS extends TypeSwitch
		{
			override def caseVoidType(t: VoidType) = ps.print("void")
			override def caseBooleanType(t: BooleanType) = ps.print("jboolean")
			override def caseByteType(t: ByteType) = ps.print("jbyte")
			override def caseCharType(t: CharType) = ps.print("jchar")
			override def caseShortType(t: ShortType) = ps.print("jshort")
			override def caseIntType(t: IntType) = ps.print("jint")
			override def caseLongType(t: LongType) = ps.print("jlong")
			override def caseFloatType(t: FloatType) = ps.print("jfloat")
			override def caseDoubleType(t: DoubleType) = ps.print("jdouble")
			
			override def caseArrayType(t: ArrayType)
			{
				ps.print("::com::cowlark::cowjac::Array< ")
				t.getElementType.apply(TS)
				ps.print(" >*")
			}
			
			override def caseRefType(t: RefType)
			{
				ps.print(className(t.getSootClass))
				ps.print("*")
			}
			
			override def defaultCase(t: Type) = assert(false)
		}
		t.apply(TS)
	}
	
	def translateFieldDeclaration(field: SootField, hps: PrintStream)
	{
		val isref = field.getType.isInstanceOf[RefLikeType]

		hps.print("\t")
		translateModifier(field, hps)
		if (isref && field.isStatic)
			hps.print("com::cowlark::cowjac::GlobalReference< ")
		translateType(field.getType, hps)
		if (isref && field.isStatic)
			hps.print(" >")
		hps.print(" ")
		hps.print(fieldName(field))
		hps.print(";\n")
	}
	
	def translateFieldDefinition(field: SootField, cps: PrintStream)
	{
		if (field.isStatic)
		{
			val isref = field.getType.isInstanceOf[RefLikeType]
			
			if (isref)
				cps.print("com::cowlark::cowjac::GlobalReference< ")
			translateType(field.getType, cps)
			if (isref)
				cps.print(" >")
				
			cps.print(" ")
			cps.print(className(field.getDeclaringClass))
			cps.print("::")
			cps.print(fieldName(field))
			cps.print(";\n")
		}
	}
	
	def translateMethodDeclaration(method: SootMethod, hps: PrintStream)
	{
		hps.print("\t")
		translateModifier(method, hps)
		
		if (!method.isPrivate && !method.isStatic)
			hps.print("virtual ")
			
		translateType(method.getReturnType, hps)
		hps.print(" ")
		hps.print(methodName(method))

		hps.print("(com::cowlark::cowjac::Stackframe*")
		
		for (to <- method.getParameterTypes)
		{
			val t = to.asInstanceOf[Type]

			hps.print(", ")
			translateType(t, hps)
		}
			
		hps.print(")")
		
		if (method.isAbstract)
			hps.print(" = 0")
		hps.print(";\n")
	}
	
	def translateMethodDefinition(method: SootMethod, ps: PrintStream)
	{
		val body = method.getActiveBody
		
		translateType(method.getReturnType, ps)
		ps.print(" ")
		ps.print(className(method.getDeclaringClass))
		ps.print("::")
		ps.print(methodName(method))
			
		ps.print("(com::cowlark::cowjac::Stackframe* parentFrame")
		
		for (i <- 0 until method.getParameterCount)
		{
			val t = method.getParameterType(i)
			
			ps.print(", ")
			translateType(t, ps)
			ps.print(" p")
			ps.print(i)
		}
		
		ps.print(")\n{\n")
		
		/* Declare stackframe structure. */

		ps.print("\tstruct frame : public com::cowlark::cowjac::Stackframe\n")
		ps.print("\t{\n");
		ps.print("\t\tframe(com::cowlark::cowjac::Stackframe* p):\n")
		ps.print("\t\t\tcom::cowlark::cowjac::Stackframe(p)\n")
	
		val reflike = body.getLocals.filter(s => s.getType.isInstanceOf[RefLikeType])
		
		ps.print("\t\t{\n")
		if (!reflike.isEmpty)
		{
			ps.print("\t\t\tmemset(&f")
			ps.print(reflike.first.getName)
			ps.print(", 0, sizeof(f")
			ps.print(reflike.first.getName)
			ps.print(") * ")
			ps.print(reflike.size)
			ps.print(");\n")
		}
		ps.print("\t\t}\n")
		
		ps.print("\n")
		
		if (!reflike.isEmpty)
		{
			ps.print("\t\tvoid mark()\n")
			ps.print("\t\t{\n")
			
			ps.print("\t\t\tmarkMany((void**) &f")
			ps.print(reflike.first.getName)
			ps.print(", ")
			ps.print(reflike.size)
			ps.print(");\n")
			
			ps.print("\t\t}\n")
		}
		
		ps.print("\n")
		ps.print("public:\n")
		
		for (local <- reflike)
		{
			val t = local.getType

			ps.print("\t\t")
			translateType(t, ps)

			ps.print(" f")
			ps.print(local.getName)
			ps.print(";\n")
		}

		ps.print("\t};\n");
		ps.print("\tframe F(parentFrame);\n")
		ps.print("\n")
		
		/* Declare locals that don't need to go in the frame. */
		
		for (local <- body.getLocals)
		{
			val t = local.getType
			if (!t.isInstanceOf[RefLikeType])
			{
				ps.print("\t")
				translateType(t, ps)
				ps.print(" j")
				ps.print(local.getName)
				ps.print(" = 0;\n")
			}
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
				ps.print(javaToCXX(t.getSootClass.getName))
			
			override def defaultCase(t: Type) = assert(false)
		}

		object VS extends AbstractJimpleValueSwitch
		{
			override def caseIntConstant(s: IntConstant) =
				ps.print(s.value)
			
			override def caseLongConstant(s: LongConstant) =
			{
				ps.print(s.value)
				ps.print("LL")
			}
			
			override def caseFloatConstant(s: FloatConstant) =
			{
				ps.print(s.value)
				ps.print("f")
			}
			
			override def caseDoubleConstant(s: DoubleConstant) =
				ps.print(s.value)
			
			override def caseStringConstant(s: StringConstant) =
			{
				ps.print("(java::lang::String*)0 /* string constant */")
			}
			
			override def caseNullConstant(s: NullConstant) =
				ps.print("0")
				
			override def caseClassConstant(s: ClassConstant) =
			{
				ps.print(className(s.value))
				ps.print("::classInit(&F)->CLASS");
			}
				
			override def caseThisRef(v: ThisRef) =
				ps.print("this")
				
			override def caseLocal(v: Local) =
			{
				if (v.getType.isInstanceOf[RefLikeType])
					ps.print("F.f")
				else
					ps.print("j")
				ps.print(v.getName)
			}
			
			override def caseInstanceFieldRef(v: InstanceFieldRef) =
			{
				v.getBase.apply(VS)
				ps.print("->f_")
				ps.print(className(v.getFieldRef.declaringClass))
				ps.print("::")
				ps.print(v.getFieldRef.name)
			}
			
			override def caseStaticFieldRef(v: StaticFieldRef) =
			{
				ps.print("<staticfield ")
				ps.print(v)
				ps.print(">")
			}
			
			override def caseArrayRef(v: ArrayRef) =
			{
				ps.print("<array ")
				ps.print(v)
				ps.print(">")
			}
			
			override def caseLengthExpr(v: LengthExpr) =
			{
				ps.print("<length ")
				ps.print(v)
				ps.print(">")
			}
				
			override def caseParameterRef(v: ParameterRef) =
			{
				ps.print("p")
				ps.print(v.getIndex)
			}
			
			override def caseCaughtExceptionRef(v: CaughtExceptionRef) =
			{
				ps.print("<caughtexception>")
			}
			
			override def caseCastExpr(v: CastExpr) =
			{
				ps.print("<cast ")
				ps.print(v)
				ps.print(">")
			}
			
			override def caseInstanceOfExpr(v: InstanceOfExpr) =
			{
				ps.print("<instanceof ")
				ps.print(v)
				ps.print(">")
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
				ps.print(v.getSymbol)
				v.getOp2.apply(VS)
			}

			private def caseBinopXExpr(v: BinopExpr, x: String) =
			{
				ps.print("::com::cowlark::cowjac::")
				ps.print(x)
				ps.print("(")
				v.getOp1.apply(VS)
				ps.print(", ")
				v.getOp2.apply(VS)
				ps.print(")")
			}
			
			override def caseNegExpr(v: NegExpr) =
			{
				ps.print("-")
				v.getOp.apply(VS)
			}
			
			override def caseNewExpr(v: NewExpr) =
			{
				ps.print("new ")
				v.getType.apply(NS)
			}
			
			override def caseNewArrayExpr(v: NewArrayExpr) =
			{
				ps.print("::com::cowlark::cowjac::Array< ")
				translateType(v.getBaseType, ps)
				ps.print(" >::Create(&F, ")
				v.getSize.apply(VS)
				ps.print(")")
			}
			
			private def parameters(v: InvokeExpr)
			{
				ps.print("(&F")
				
				for (arg <- v.getArgs)
				{
					ps.print(", ")
					arg.apply(VS)
				}
				
				ps.print(")")
			}
			
			override def caseInterfaceInvokeExpr(v: InterfaceInvokeExpr) =
				caseInstanceInvokeExpr(v)
				
			override def caseVirtualInvokeExpr(v: VirtualInvokeExpr) =
				caseInstanceInvokeExpr(v)
				
			def caseInstanceInvokeExpr(v: InstanceInvokeExpr) =
			{
				if (!notnull)
					ps.print("com::cowlark::cowjac::NullCheck(")
				v.getBase.apply(VS)
				if (!notnull)
					ps.print(")")
					
				ps.print("->")
				ps.print(methodName(v.getMethodRef))
				parameters(v)
			}
				
			override def caseSpecialInvokeExpr(v: SpecialInvokeExpr) =
			{
				if (!notnull)
					ps.print("com::cowlark::cowjac::NullCheck(")
				v.getBase.apply(VS)
				if (!notnull)
					ps.print(")")
					
				ps.print("->")
				ps.print(className(v.getMethodRef.declaringClass))
				ps.print("::")
				ps.print(methodName(v.getMethodRef))
					
				parameters(v)
			}
			
			override def caseStaticInvokeExpr(v: StaticInvokeExpr) =
			{
				ps.print(className(v.getMethodRef.declaringClass))
				ps.print("::")
				ps.print(methodName(v.getMethodRef))
				
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
				ps.print("\treturn ")
				s.getOp.apply(VS)
				ps.print(";\n")
			}
			
			override def caseReturnVoidStmt(s: ReturnVoidStmt) =
				ps.print("\treturn;\n")
			
			override def caseIfStmt(s: IfStmt) =
			{
				ps.print("\tif (")
				s.getCondition.apply(VS)
				ps.print(") goto ")
				ps.print(label(s.getTarget))
				ps.print(";\n")
			}
				
			override def caseInvokeStmt(s: InvokeStmt) =
			{
				ps.print("\t")
				s.getInvokeExpr.apply(VS)
				ps.print(";\n")
			}
				
			def caseDefinitionStmt(s: DefinitionStmt) =
			{
				ps.print("\t")
				s.getLeftOp.apply(VS)
				ps.print(" = ")
				s.getRightOp.apply(VS)
				ps.print(";\n")
			}
			
			override def caseThrowStmt(s: ThrowStmt) =
			{
				ps.print("\tthrow ")
				s.getOp.apply(VS)
				ps.print(";\n")
			}
			
			override def caseGotoStmt(s: GotoStmt) =
			{
				ps.print("\tgoto ")
				ps.print(label(s.getTarget))
				ps.print(";\n")
			}
			
			override def caseEnterMonitorStmt(s: EnterMonitorStmt) =
			{
				ps.print("\t")
					
				if (!notnull)
					ps.print("com::cowlark::cowjac::NullCheck(")
				s.getOp.apply(VS)
				if (!notnull)
					ps.print(")")
					
				ps.print("->enterMonitor();\n")
			}
			
			override def caseExitMonitorStmt(s: ExitMonitorStmt) =
			{
				ps.print("\t")
					
				if (!notnull)
					ps.print("com::cowlark::cowjac::NullCheck(")
				s.getOp.apply(VS)
				if (!notnull)
					ps.print(")")
					
				ps.print("->leaveMonitor();\n")
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
			{
				ps.print(label(unit))
				ps.print(":\n")
			}

			val tag = unit.getTag("NullCheckTag").asInstanceOf[NullCheckTag]
			notnull = (tag != null) && !tag.needCheck()
			unit.apply(SS)
					
			oldunit = unit
		}
		
		ps.print("}\n\n")
	}
	
	private def forwardDeclare(sootclass: SootClass, ps: PrintStream)
	{
		val nslevels = sootclass.getName.split('.')
		for (i <- 0 to nslevels.length-2)
			ps.print("namespace "+nslevels(i)+" { ")
		
		ps.print("class ")
		ps.print(sootclass.getJavaStyleName)
		ps.print("; ")
		
		for (i <- 0 to nslevels.length-2)
			ps.print("}")
		ps.print("\n")
	}
	
	def translate(sootclass: SootClass, cps: PrintStream, hps: PrintStream)
	{
		val jname = sootclass.getName()
		val cxxname = javaToCXX(jname)
		val headername = reformName(jname, "_").toUpperCase() + "_H"
		
		hps.print("#ifndef "+headername+"\n")
		hps.print("#define "+headername+"\n")
		
		cps.print("#include \"cowjac.h\"\n")
		cps.print("#include \"cowjacarray.h\"\n")
		
		hps.print("\n")
		val dependencies = getDependencies(sootclass)
		for (d <- dependencies)
		{
			forwardDeclare(d, hps)
			cps.print("#include \"")
			cps.print(d.getName)
			cps.print(".h\"\n")
		}
		
		hps.print("\n")
		cps.print("\n")
		
		val nslevels = jname.split('.')
		for (i <- 0 to nslevels.length-2)
			hps.print("namespace "+nslevels(i)+" {\n")
		
		hps.print("\n")
		
		var superclasses = Vector.empty[SootClass]
		if (jname != "java.lang.Object")
			superclasses = superclasses :+ sootclass.getSuperclass

		for (s <- superclasses)
		{
			hps.print("#include \"")
			hps.print(s.getName)
			hps.print(".h\"\n")
		}
		
		hps.print("class " + sootclass.getJavaStyleName)
		if (!superclasses.isEmpty)
		{
			val superclassnames = superclasses.map((c: SootClass) => "public " + javaToCXX(c.getName))
			hps.print(" : ")
			hps.print(superclassnames.reduceLeft(_ + ", " + _))
		}
		else
			hps.print(" : public com::cowlark::cowjac::Object")
		
		hps.print("\n{\n")
		
		hps.print("\t/* Class management */\n")
		cps.print("/* Class management */\n")
		
		hps.print("\tprivate: static bool initialised;\n")
		cps.print("bool ")
		cps.print(className(sootclass))
		cps.print("::initialised = false;\n")
		
		hps.print("\tpublic: static ::java::lang::Class* CLASS;\n")
		hps.print("\tpublic: static ")
		hps.print(className(sootclass))
		hps.print("* classInit(com::cowlark::cowjac::Stackframe*);\n")
		hps.print("\n")
		
		hps.print("\t/* Field declarations */\n")
		cps.print("\n/* Field definitions */\n")
		for (f <- sootclass.getFields)
		{
			translateFieldDeclaration(f, hps)
			translateFieldDefinition(f, cps)
		}
		
		hps.print("\n")
		
		hps.print("\t/* Method declarations */\n")
		cps.print("\n/* Method definitions */\n")
		
		/* Emit constructor and destructor */
		
		hps.print("\tpublic: ")
		hps.print(sootclass.getShortName)
		hps.print("();\n")
		hps.print("\tpublic: virtual ~")
		hps.print(sootclass.getShortName)
		hps.print("() {};\n")
				
		/* Ordinary methods */
				
		for (m <- sootclass.getMethods)
		{
			translateMethodDeclaration(m, hps)
			if (m.hasActiveBody)
				translateMethodDefinition(m, cps)
		}
		
		hps.print("};\n")
		hps.print("\n")
		
		for (i <- 0 to nslevels.length-2)
			hps.print("} /* namespace "+nslevels(i)+" */\n")
		
		hps.print("#endif\n")
	}
}