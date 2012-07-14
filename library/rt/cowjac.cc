#include <assert.h>
#include "cowjac.h"
#include "cowjacarray.h"
#include "cowjacclass.h"
#include "test.Main.h"
#include "org.apache.harmony.luni.util.FloatingPointParser.h"
#include "org.apache.harmony.luni.util.NumberConverter.h"
#include "java.lang.Throwable.h"
#include "java.lang.Float.h"
#include "java.lang.Double.h"

using com::cowlark::cowjac::SystemLock;
using com::cowlark::cowjac::Stackframe;
using com::cowlark::cowjac::ContainsGlobalReferences;
using com::cowlark::cowjac::ContainsReferences;
using com::cowlark::cowjac::BaseArray;
using com::cowlark::cowjac::ObjectArray;
using com::cowlark::cowjac::PrimitiveClass;
using java::lang::Class;

ContainsGlobalReferences* ContainsGlobalReferences::_first = NULL;

ContainsGlobalReferences::ContainsGlobalReferences()
{
	_prev = NULL;
	_next = _first;
	_first = this;
}

ContainsGlobalReferences::~ContainsGlobalReferences()
{
	if (_prev)
		_prev->_next = _next;
	if (_next)
		_next->_prev = _prev;
	if (_first == this)
		_first = _next;
}

void Stackframe::markMany(ContainsReferences** p, unsigned count)
{
	while (count--)
	{
		if (*p)
			(*p)->mark();
		p++;
	}
}

SystemLock::SystemLock()
{
}

SystemLock::~SystemLock()
{
}

void com::cowlark::cowjac::ThrowNullPointerException()
{
	assert(false);
}

void com::cowlark::cowjac::CastFailed(Stackframe* F)
{
	assert(false);
}

com::cowlark::cowjac::Object::Object():
		_marked(false),
		_immutable(false)
{
}

com::cowlark::cowjac::Object::~Object()
{
}

void com::cowlark::cowjac::Object::mark()
{
	if (!_marked)
	{
		_marked = true;
		markImpl();
	}
}

void com::cowlark::cowjac::Object::enterMonitor()
{
}

void com::cowlark::cowjac::Object::leaveMonitor()
{
}

/* Arrays */

BaseArray::BaseArray(Class* arrayClass, jint length, jint elementLength):
		_data(new jbyte[length * elementLength]),
		_length(length),
		_elementLength(elementLength),
		_class(arrayClass)
{
	memset(_data, 0, _length * _elementLength);
}

BaseArray::~BaseArray()
{
	delete [] _data;
}

void BaseArray::boundsCheck(Stackframe* pF, jint index) const
{
	if ((index < 0) || (index >= _length))
		assert(false);
}

void ObjectArray::markImpl()
{
	for (int i = 0; i < length(); i++)
	{
		java::lang::Object* o = getUnchecked(i);
		if (o)
			o->mark();
	}
}

/* Native implementations of Java methods. */

jboolean java::lang::Object::equals(Stackframe* pF, java::lang::Object* other)
{
	return other == this;
}

jint java::lang::Object::hashCode(Stackframe* pF)
{
	return (intptr_t)this >> 3;
}

java::lang::Class* java::lang::Object::getClass(Stackframe* pF)
{
	assert(false);
}

jdouble org::apache::harmony::luni::util::FloatingPointParser::parseDouble(
		Stackframe* pF, java::lang::String* s)
{
	assert(false);
	return 0.0;
}

java::lang::Throwable* java::lang::Throwable::fillInStackTrace(Stackframe* pF)
{
	return this;
}

jint java::lang::Float::floatToIntBits(Stackframe* pF, jfloat f)
{
	if (f != f)
		return 0x7FC00000;
	return floatToRawIntBits(pF, f);
}

jint java::lang::Float::floatToRawIntBits(Stackframe* pF, jfloat f)
{
	return *reinterpret_cast<jint*>(&f);
}

jlong java::lang::Double::doubleToLongBits(Stackframe* pF, jdouble f)
{
	if (f != f)
		return 0x7ff8000000000000LL;
	return doubleToRawLongBits(pF, f);
}

jlong java::lang::Double::doubleToRawLongBits(Stackframe* pF, jdouble f)
{
	return *reinterpret_cast<jlong*>(&f);
}

java::lang::String* org::apache::harmony::luni::util::NumberConverter::convert(Stackframe* pF, jdouble d)
{
	assert(false);
	return NULL;
}

int main(int argc, const char* argv[])
{
	Stackframe frame;

	com::cowlark::cowjac::PrimitiveBooleanClassConstant
		= new PrimitiveClass(&frame, "boolean");
	com::cowlark::cowjac::PrimitiveByteClassConstant
		= new PrimitiveClass(&frame, "byte");
	com::cowlark::cowjac::PrimitiveCharClassConstant
		= new PrimitiveClass(&frame, "char");
	com::cowlark::cowjac::PrimitiveShortClassConstant
		= new PrimitiveClass(&frame, "short");
	com::cowlark::cowjac::PrimitiveIntClassConstant
		= new PrimitiveClass(&frame, "int");
	com::cowlark::cowjac::PrimitiveLongClassConstant
		= new PrimitiveClass(&frame, "long");
	com::cowlark::cowjac::PrimitiveFloatClassConstant
		= new PrimitiveClass(&frame, "float");
	com::cowlark::cowjac::PrimitiveDoubleClassConstant
		= new PrimitiveClass(&frame, "double");

	test::Main::main(&frame, NULL);
	return 0;
}
