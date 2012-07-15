#ifndef COWJACCLASS_H
#define COWJACCLASS_H

#include "java.lang.Class.h"

namespace com {
namespace cowlark {
namespace cowjac {

/* Shim between Java-style objects and C++-style objects. */

class ShimClass : public ::java::lang::Class
{
public:
	ShimClass(Stackframe* F);
};

/* Type of class representing simple objects. */

class SimpleClass : public ShimClass
{
public:
	SimpleClass(Stackframe* F, const char* name);
};

/* Type of class representing primitives. */

class PrimitiveClass : public ShimClass
{
public:
	PrimitiveClass(Stackframe* F, const char* name);

	jboolean isPrimitive(::com::cowlark::cowjac::Stackframe* F) { return true; }
};

extern PrimitiveClass* PrimitiveBooleanClassConstant;
extern PrimitiveClass* PrimitiveByteClassConstant;
extern PrimitiveClass* PrimitiveCharClassConstant;
extern PrimitiveClass* PrimitiveShortClassConstant;
extern PrimitiveClass* PrimitiveIntClassConstant;
extern PrimitiveClass* PrimitiveLongClassConstant;
extern PrimitiveClass* PrimitiveFloatClassConstant;
extern PrimitiveClass* PrimitiveDoubleClassConstant;

/* Type of class representing an array. */

class ArrayClass : public ShimClass
{
public:
	ArrayClass(::com::cowlark::cowjac::Stackframe* F, ::java::lang::Class* element);

	::java::lang::Class* getComponentType(::com::cowlark::cowjac::Stackframe* F)
	{
		return _element;
	}

	jboolean isArray(::com::cowlark::cowjac::Stackframe* F) { return true; }

private:
	::java::lang::Class* _element;
};

}}}

#endif
