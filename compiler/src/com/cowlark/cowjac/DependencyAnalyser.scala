package com.cowlark.cowjac
import scala.collection.JavaConversions._

import soot.jimple.FieldRef
import soot.jimple.StaticInvokeExpr
import soot.ArrayType
import soot.BooleanType
import soot.ByteType
import soot.CharType
import soot.DoubleType
import soot.FloatType
import soot.IntType
import soot.LongType
import soot.NullType
import soot.RefType
import soot.ShortType
import soot.SootClass
import soot.Type
import soot.TypeSwitch
import soot.VoidType

trait DependencyAnalyser
{
	def getDependencies(sootclass: SootClass): Set[SootClass] =
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
}
