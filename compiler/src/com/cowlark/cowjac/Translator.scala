package com.cowlark.cowjac
import soot.SootClass
import java.io.PrintStream
import scala.collection.immutable.HashMap
import scala.collection.JavaConversions._
import soot.SootField

object Translator
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
	
	def translateFieldDeclaration(field: SootField, hps: PrintStream)
	{
		if (field.isPrivate)
			hps.print("private: ")
		else if (field.isProtected)
			hps.print("protected: ")
		else
			hps.print("public: ")
			
		if (field.isStatic)
			hps.print("static ")
			
		hps.print(field.getSignature + "\n")
	}
	
	def translate(sootclass: SootClass, cps: PrintStream, hps: PrintStream)
	{
		val jname = sootclass.getName()
		val cxxname = javaToCXX(jname)
		val headername = reformName(jname, "_").toUpperCase() + "_H"
		
		hps.print("#ifndef "+headername+"\n")
		hps.print("#define "+headername+"\n")
		
		hps.print("\n")
		val nslevels = jname.split('.')
		for (i <- 0 to nslevels.length-2)
			hps.print("namespace "+nslevels(i)+" {\n")
		
		hps.print("\n")
		hps.print("class " + sootclass.getJavaStyleName)
		
		var superclasses = Vector.empty[SootClass]
		if (jname != "java.lang.Object")
			superclasses = superclasses :+ sootclass.getSuperclass
		
		if (!superclasses.isEmpty)
		{
			val superclassnames = superclasses.map((c: SootClass) => "public " + javaToCXX(c.getName))
			hps.print(" : ")
			hps.print(superclassnames.reduceLeft(_ + ", " + _))
		}
		
		hps.print("\n{\n")
		
		for (f <- sootclass.getFields)
			translateFieldDeclaration(f, hps)
			
		hps.print("};\n")
		hps.print("\n")
		
		for (i <- 0 to nslevels.length-2)
			hps.print("} /* namespace "+nslevels(i)+" */\n")
		
		hps.print("#endif\n")
	}
}