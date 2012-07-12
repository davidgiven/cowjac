package com.cowlark.cowjac
import soot.SootClass
import scala.collection.JavaConversions._
import soot.SootMethod

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
}
