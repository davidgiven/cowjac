package com.cowlark.cowjac
import soot.SootClass
import java.io.PrintStream
import scala.collection.immutable.HashMap
import scala.collection.JavaConversions._
import soot.SootField
import soot.ClassMember
import soot.Type
import soot.TypeSwitch
import soot.BooleanType
import soot.DoubleType
import soot.FloatType
import soot.IntType
import soot.LongType
import soot.VoidType
import soot.CharType
import soot.ShortType
import soot.ByteType
import soot.RefLikeType
import soot.RefType
import soot.SootMethod
import soot.ArrayType

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
			hps.print("__init__")
		else
			hps.print(method.getName)
		hps.print("(com::cowlark::cowjac::Context*")
		
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
		cps.print("/* Field definitions */\n")
		for (f <- sootclass.getFields)
		{
			translateFieldDeclaration(f, hps)
			translateFieldDefinition(f, cps)
		}
		
		hps.print("\n")
		
		hps.print("\t/* Method declarations */\n")
		cps.print("/* Method definitions */\n")
		
		/* Emit constructor and destructor */
		
		hps.print("\t")
		hps.print(sootclass.getShortName)
		hps.print("();\n")
		hps.print("\tvirtual ~")
		hps.print(sootclass.getShortName)
		hps.print("();\n")
				
		/* Ordinary methods */
				
		for (m <- sootclass.getMethods)
		{
			translateMethodDeclaration(m, hps)
		}
		
		hps.print("};\n")
		hps.print("\n")
		
		for (i <- 0 to nslevels.length-2)
			hps.print("} /* namespace "+nslevels(i)+" */\n")
		
		hps.print("#endif\n")
	}
}