package com.cowlark.cowjac
import scala.collection.JavaConversions._
import soot.SootClass
import soot.RefType
import soot.TypeSwitch
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
import soot.SourceLocator
import soot.jimple.JimpleBody
import soot.NullType

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
			
		classes = classes ++ sootclass.getInterfaces
			
		for (f <- sootclass.getFields)
			addType(f.getType)
			
		for (m <- sootclass.getMethods)
		{
			addType(m.getReturnType)
			for (t <- m.getParameterTypes)
				addType(t.asInstanceOf[Type])
				
			classes = classes ++ m.getExceptions
			
			if (m.hasActiveBody)
			{
				val body = m.getActiveBody
				
				for (local <- body.getLocals)
					addType(local.getType)
					
				for (value <- body.getUseAndDefBoxes)
					addType(value.getValue.getType)
			}
		}
		
		return classes
	}
}
