package com.cowlark.cowjac
import soot.SootClass
import scala.collection.JavaConversions._
import soot.SootMethod
import soot.RefType
import soot.jimple.FieldRef
import soot.jimple.StaticInvokeExpr
import soot.TypeSwitch
import soot.NullType
import soot.LongType
import soot.IntType
import soot.ArrayType
import soot.DoubleType
import soot.CharType
import soot.BooleanType
import soot.VoidType
import soot.ByteType
import soot.FloatType
import soot.Type
import soot.ShortType

trait SootExtensions
{
	def getAllInterfaces(sootclass: SootClass): Set[SootClass] =
	{
		var set = Set.empty[SootClass]
		
		def add(sootclass: SootClass)
		{
			val ii = sootclass.getInterfaces
			for (i <- ii)
			{
				if (!set.contains(i))
				{
					set += i
					add(i)
				}
			}
			
			if (sootclass.hasSuperclass)
				add(sootclass.getSuperclass)
		}
		
		add(sootclass)
		return set
	}
	
	def getAllRealMethods(sootclass: SootClass): Set[SootMethod] =
	{
		var set = Set.empty[SootMethod]
		set ++= sootclass.getMethods
		
		if (sootclass.hasSuperclass)
			set ++= getAllRealMethods(sootclass.getSuperclass)
			
		return set
	}
	
	def getAllInterfaceMethods(sootclass: SootClass): Set[SootMethod] =
	{
		var seen = Set.empty[SootClass]
		var set = Set.empty[SootMethod]
		
		def add(sootclass: SootClass)
		{
			set ++= sootclass.getMethods
			
			val ii = sootclass.getInterfaces
			for (i <- ii)
			{
				if (!seen.contains(i))
				{
					seen += i
					add(i)
				}
			}
			
			if (sootclass.hasSuperclass)
			{
				val sc = sootclass.getSuperclass
				if (sc.isInterface)
					add(sc)
			}
		}
		
		add(sootclass)
		return set
	}
	
	def getMethodRecursively(sootclass: SootClass, signature: String): SootMethod =
	{
		if (sootclass.declaresMethod(signature))
			return sootclass.getMethod(signature)
		return getMethodRecursively(sootclass.getSuperclass, signature)
	}
	
	def getClassDependencies(sootclass: SootClass): Set[SootClass] =
	{
		var classes = Set.empty[SootClass]
		
		object TS extends TypeSwitch
		{
			override def caseVoidType(t: VoidType) = {}
			override def caseBooleanType(t: BooleanType) = {}
			override def caseByteType(t: ByteType) = {}
			override def caseCharType(t: CharType) = {}
			override def caseShortType(t: ShortType) = {}
			override def caseIntType(t: IntType) = {}
			override def caseLongType(t: LongType) = {}
			override def caseFloatType(t: FloatType) = {}
			override def caseDoubleType(t: DoubleType) = {}
			override def caseNullType(t: NullType) = {}
			
			override def caseArrayType(t: ArrayType) = t.getElementType.apply(TS)
			
			override def caseRefType(t: RefType) = classes += t.getSootClass
			
			override def defaultCase(t: Type) = assert(false)
		}

		def addType(t: Type) = t.apply(TS)
		
		classes += sootclass
		
		if (sootclass.getName != "java.lang.Object")
			classes = classes + sootclass.getSuperclass
			
		classes ++= sootclass.getInterfaces
			
		for (f <- sootclass.getFields)
			addType(f.getType)
			
		for (m <- sootclass.getMethods)
		{
			addType(m.getReturnType)
			for (t <- m.getParameterTypes)
				addType(t.asInstanceOf[Type])
				
			classes ++= m.getExceptions
			
			if (m.hasActiveBody)
			{
				val body = m.getActiveBody
				
				for (local <- body.getLocals)
					addType(local.getType)
					
				for (valueref <- body.getUseAndDefBoxes)
				{
					val value = valueref.getValue
					addType(value.getType)
					
					if (value.isInstanceOf[FieldRef])
						classes += value.asInstanceOf[FieldRef].getFieldRef.declaringClass
					else if (value.isInstanceOf[StaticInvokeExpr])
						classes += value.asInstanceOf[StaticInvokeExpr].getMethodRef.declaringClass
				}
			}
		}
		
		return classes
	}
	
	def isMethodPureImpl(method: SootMethod): Boolean =
	{
		var sootclass = method.getDeclaringClass
		var pure = false
			
		if (sootclass.isInterface || method.isAbstract)
		{
			/* This method might be pure (in the C++ sense). That is, it's
			 * being declared but not being implemented at this level in
			 * the hierarchy. We now need to recursively scan the
			 * superclasses of this class looking for a method with the
			 * same signature. If one is not found, we're pure. */
			
			val subsignature = method.getSubSignature
			var processed = Set.empty[SootClass]
			pure = true
			def scan(c: SootClass)
			{
				if (processed.contains(c))
					return
				processed += c
				
				if (c.declaresMethod(subsignature))
				{
					pure = false
					return
				}
				
				if (c.hasSuperclass)
					scan(c.getSuperclass)
				for (i <- c.getInterfaces)
					scan(i)
			}
			
			if (sootclass.hasSuperclass)
				scan(sootclass.getSuperclass)
			for (i <- sootclass.getInterfaces)
				scan(i)
		}
		
		return pure
	}
	
	var isMethodPure = Memoize(isMethodPureImpl)
}
