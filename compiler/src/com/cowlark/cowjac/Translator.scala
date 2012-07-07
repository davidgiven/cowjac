package com.cowlark.cowjac
import java.io.PrintStream

import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.JavaConversions.collectionAsScalaIterable
import scala.collection.immutable.HashMap

import com.cowlark.cowjac.DependencyAnalyser

import soot.jimple.AbstractJimpleValueSwitch
import soot.jimple.AbstractStmtSwitch
import soot.jimple.IdentityStmt
import soot.jimple.AssignStmt
import soot.jimple.DefinitionStmt
import soot.jimple.ReturnVoidStmt
import soot.jimple.ReturnStmt
import soot.jimple.InvokeStmt
import soot.jimple.IfStmt
import soot.toolkits.graph.BriefUnitGraph
import soot.ArrayType
import soot.BooleanType
import soot.ByteType
import soot.CharType
import soot.ClassMember
import soot.DoubleType
import soot.FloatType
import soot.IntType
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
import soot.Local
import soot.jimple.ThisRef
import soot.jimple.NewExpr
import soot.jimple.AddExpr
import soot.jimple.GeExpr
import soot.jimple.BinopExpr
import soot.jimple.VirtualInvokeExpr
import soot.jimple.SpecialInvokeExpr
import soot.jimple.InvokeExpr
import soot.jimple.ParameterRef
import soot.jimple.IntConstant
import soot.jimple.StringConstant

object Translator extends DependencyAnalyser
{
	private var namecache = HashMap[String, String]()
	
	def reformName(jname: String, separator: String): String =
		jname.split('.').reduceLeft(_ + separator + _)
		
	def javaToCXX(jname: String): String =
	{
		val n = namecache.get(jname)
		if (n != None)
			return n.get
		
		val cxxname = reformName(jname, "::")
		namecache = namecache + (jname -> cxxname)
		return cxxname
	}
	
	def translateModifier(cm: ClassMember, ps: PrintStream)
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
	
	def translateType(t: Type, ps: PrintStream)
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
				ps.print("com::cowlark::cowjac::Array<")
				t.getElementType.apply(TS)
				ps.print(">*")
			}
			
			override def caseRefType(t: RefType)
			{
				ps.print(javaToCXX(t.getSootClass.getName))
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
		if (isref)
			hps.print("com::cowlark::cowjac::GlobalReference<")
		translateType(field.getType, hps)
		if (isref)
			hps.print(">")
		hps.print(" ")
		hps.print(field.getName)
		hps.print(";\n")
	}
	
	def translateFieldDefinition(field: SootField, cps: PrintStream)
	{
		if (field.isStatic)
		{
			val isref = field.getType.isInstanceOf[RefLikeType]
			
			if (isref)
				cps.print("com::cowlark::cowjac::GlobalReference<")
			translateType(field.getType, cps)
			if (isref)
				cps.print(">")
				
			cps.print(" ")
			cps.print(javaToCXX(field.getDeclaringClass.getName))
			cps.print("::")
			cps.print(field.getName)
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
		if (method.getName == "<init>")
			hps.print("___init")
		else
			hps.print(method.getName)
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
		ps.print(javaToCXX(method.getDeclaringClass.getName))
		ps.print("::")
		if (method.getName == "<init>")
			ps.print("___init")
		else
			ps.print(method.getName)
			
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

		val hasFrame = body.getLocals.exists(
				local => local.getType.isInstanceOf[RefLikeType])

		if (hasFrame)
		{
			ps.print("\tstruct frame : public com::cowlark::cowjac::Stackframe\n")
			ps.print("\t{\n");
			ps.print("\t\tframe(com::cowlark::cowjac::Stackframe* p):\n")
			ps.print("\t\t\tcom::cowlark::cowjac::Stackframe(p)\n")
			
			for (local <- body.getLocals)
			{
				val t = local.getType
				if (t.isInstanceOf[RefLikeType])
				{
					ps.print("\t\t\t, f")
					ps.print(local.getName)
					ps.print("(0)\n")
				}
			}
			
			ps.print("\t\t{}\n")
			ps.print("\n")
			
			for (local <- body.getLocals)
			{
				val t = local.getType
				if (t.isInstanceOf[RefLikeType])
				{
					ps.print("\t\t")
					translateType(t, ps)
	
					ps.print(" f")
					ps.print(local.getName)
					ps.print(";\n")
				}
			}

			ps.print("\n")
			ps.print("\t\tvoid ___mark()\n")
			ps.print("\t\t{\n")
			
			for (local <- body.getLocals)
			{
				val t = local.getType
				if (t.isInstanceOf[RefLikeType])
				{
					ps.print("\t\t\tif (f")
					ps.print(local.getName)
					ps.print(") f")
					ps.print(local.getName)
					ps.print("->___mark();\n")
				}
			}

			ps.print("\t\t}\n")
			ps.print("\t};\n");
			ps.print("\tframe F(parentFrame);\n")
			ps.print("\n")
		}
		
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
			
			override def caseStringConstant(s: StringConstant) =
			{
				ps.print("(java::lang::String*)0 /* string constant */")
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
			
			override def caseParameterRef(v: ParameterRef) =
			{
				ps.print("p")
				ps.print(v.getIndex)
			}
			
			override def caseAddExpr(v: AddExpr) = caseBinopExpr(v)
			override def caseGeExpr(v: GeExpr) = caseBinopExpr(v)
			
			def caseBinopExpr(v: BinopExpr) =
			{
				v.getOp1.apply(VS)
				ps.print(v.getSymbol)
				v.getOp2.apply(VS)
			}
			
			override def caseNewExpr(v: NewExpr) =
			{
				ps.print("new ")
				v.getType.apply(NS)
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
			
			override def caseVirtualInvokeExpr(v: VirtualInvokeExpr) =
			{
				v.getBase.apply(VS)
				ps.print("->")
				ps.print(v.getMethodRef.name)
				parameters(v)
			}
				
			override def caseSpecialInvokeExpr(v: SpecialInvokeExpr) =
			{
				v.getBase.apply(VS)
				ps.print("->")
				ps.print(javaToCXX(v.getMethodRef.declaringClass.getName))
				ps.print("::")
				if (v.getMethodRef.name == "<init>")
					ps.print("___init")
				else
					ps.print(v.getMethodRef.name)
					
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