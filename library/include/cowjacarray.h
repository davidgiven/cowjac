#ifndef COWJACARRAY_H
#define COWJACARRAY_H

#include "java.lang.Object.h"
#include "java.lang.Class.h"

namespace com {
namespace cowlark {
namespace cowjac {

class BaseArray : public java::lang::Object
{
public:
	BaseArray(::java::lang::Class* arrayClass, jint size, jint elementLength);
	BaseArray(::java::lang::Class* arrayClass, jint size, jint elementLength,
			void* ptr);
	virtual ~BaseArray();

	jint length() const
	{
		return _length;
	}

	public: ::java::lang::Class* getClass(::com::cowlark::cowjac::Stackframe* F)
	{ return _class; }

	protected: void* ptr(jint index, jint elementLength) const
	{
		return (void*)(_data + index*elementLength);
	}


	public: void boundsCheck(::com::cowlark::cowjac::Stackframe* f, jint index) const;

private:
	jbyte* _data;
	jint _length;
	jint _elementLength;
	::java::lang::Class* _class;

	jboolean _external : 1;
};

template <class T> class PrimitiveArray : public BaseArray
{
public:
	PrimitiveArray(::java::lang::Class* arrayClass, jint length):
		BaseArray(arrayClass, length, sizeof(T))
	{
	}

	PrimitiveArray(::java::lang::Class* arrayClass, jint length, void* ptr):
		BaseArray(arrayClass, length, sizeof(T), ptr)
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
	ScalarArray(::com::cowlark::cowjac::Stackframe* parentFrame,
			::java::lang::Class* arrayClass, jint length):
		PrimitiveArray<T>(arrayClass, length)
	{
	}

	ScalarArray(::com::cowlark::cowjac::Stackframe* parentFrame,
			::java::lang::Class* arrayClass, jint length, void* ptr):
		PrimitiveArray<T>(arrayClass, length, ptr)
	{
	}

	void* ptr(jint index) const
	{
		return PrimitiveArray<T>::ptr(index, sizeof(T));
	}
};

/* Object array */

class ObjectArray : public PrimitiveArray< ::java::lang::Object* >
{
public:
	ObjectArray(::com::cowlark::cowjac::Stackframe* parentFrame,
			::java::lang::Class* arrayClass, jint length):
		PrimitiveArray< ::java::lang::Object* >(arrayClass, length)
	{
	}

	void markImpl();
};

}}}

#endif
