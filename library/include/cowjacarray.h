#ifndef COWJACARRAY_H
#define COWJACARRAY_H

#include "java.lang.Object.h"

namespace com {
namespace cowlark {
namespace cowjac {

class BaseArray : public java::lang::Object
{
public:
	BaseArray(jint size, jint elementLength);
	virtual ~BaseArray();

	jint length() const
	{
		return _length;
	}

protected:
	void* ptr(jint index, jint elementLength) const
	{
		return (void*)(_data + index*elementLength);
	}

	void boundsCheck(::com::cowlark::cowjac::Stackframe* f, jint index) const;

private:
	jbyte* _data;
	jint _length;
	jint _elementLength;
};

template <class T> class PrimitiveArray : public BaseArray
{
public:
	PrimitiveArray(jint length):
		BaseArray(length, sizeof(T))
	{
	}

	void set(::com::cowlark::cowjac::Stackframe* F, jint index, T value)
	{
		boundsCheck(F, index);
		setUnchecked(index, value);
	}

	void setUnchecked(jint index, T value)
	{
		T& ref = *(T*) ptr(index, sizeof(T));
		ref = value;
	}

	T get(::com::cowlark::cowjac::Stackframe* F, jint index) const
	{
		boundsCheck(F, index);
		return getUnchecked(index);
	}

	T getUnchecked(jint index) const
	{
		return *(T*) ptr(index, sizeof(T));
	}
};

/* Primitive arrays */

template <class T> class ScalarArray : public PrimitiveArray<T>
{
public:
	ScalarArray(::com::cowlark::cowjac::Stackframe* parentFrame, jint length):
		PrimitiveArray<T>(length)
	{
	}
};

/* Object array */

class ObjectArray : public PrimitiveArray< ::java::lang::Object* >
{
public:
	ObjectArray(::com::cowlark::cowjac::Stackframe* parentFrame, jint length):
		PrimitiveArray< ::java::lang::Object* >(length)
	{
	}

	void markImpl();
};

}}}

#endif
