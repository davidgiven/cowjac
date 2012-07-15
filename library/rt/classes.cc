/* cowjac © 2012 David Given
 * This file is licensed under the Simplified BSD license. Please see
 * COPYING.cowjac for the full text.
 */

#include <assert.h>
#include "cowjac.h"
#include "cowjacarray.h"
#include "cowjacclass.h"
#include "java.lang.reflect.Array.h"

using com::cowlark::cowjac::ShimClass;
using com::cowlark::cowjac::SimpleClass;
using com::cowlark::cowjac::PrimitiveClass;
using com::cowlark::cowjac::ArrayClass;
using com::cowlark::cowjac::Stackframe;
using com::cowlark::cowjac::SystemLock;
using java::lang::Class;

PrimitiveClass* com::cowlark::cowjac::PrimitiveBooleanClassConstant;
PrimitiveClass* com::cowlark::cowjac::PrimitiveByteClassConstant;
PrimitiveClass* com::cowlark::cowjac::PrimitiveCharClassConstant;
PrimitiveClass* com::cowlark::cowjac::PrimitiveShortClassConstant;
PrimitiveClass* com::cowlark::cowjac::PrimitiveIntClassConstant;
PrimitiveClass* com::cowlark::cowjac::PrimitiveLongClassConstant;
PrimitiveClass* com::cowlark::cowjac::PrimitiveFloatClassConstant;
PrimitiveClass* com::cowlark::cowjac::PrimitiveDoubleClassConstant;

ShimClass::ShimClass(Stackframe* F)
{
	makeImmutable();
	m__3cinit_3e_5f_28_29V(F);
}

SimpleClass::SimpleClass(Stackframe* F, const char* name):
		ShimClass(F)
{
}

PrimitiveClass::PrimitiveClass(Stackframe* F, const char* name):
		ShimClass(F)
{
}

Class* Class::getArrayType(Stackframe* F)
{
	if (!_arrayType)
	{
		SystemLock lock;
		if (!_arrayType)
			_arrayType = new ArrayClass(F, this);
	}

	return _arrayType;
}

jboolean Class::isInstance(Stackframe* F, java::lang::Object* object)
{
	assert(false);
}

ArrayClass::ArrayClass(Stackframe* F, ::java::lang::Class* element):
		ShimClass(F),
		_element(element)
{
}

java::lang::Object* java::lang::reflect::Array::newInstance
		(Stackframe* F, java::lang::Class* c, int size)
{
	assert(false);
	return 0;
}
