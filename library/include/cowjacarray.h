#ifndef COWJACARRAY_H
#define COWJACARRAY_H

#include "java.lang.Object.h"

namespace com {
namespace cowlark {
namespace cowjac {

class BaseArray : public java::lang::Object
{
public:
	BaseArray(::com::cowlark::cowjac::Stackframe* parentFrame,
			jint size, jint elementLength);
	virtual ~BaseArray();

protected:
	void* ptr(::com::cowlark::cowjac::Stackframe* F, jint index,
			jint elementLength)
	{
		return (void*)(_data + index*elementLength);
	}

	void boundsCheck(::com::cowlark::cowjac::Stackframe* f, jint index);

private:
	jbyte* _data;
	jint _length;
	jint _elementLength;
};

template <class T> class Array : public BaseArray
{
public:
	Array(::com::cowlark::cowjac::Stackframe* parentFrame, jint length):
		BaseArray(parentFrame, length, sizeof(T))
	{
	}

	T& ref(::com::cowlark::cowjac::Stackframe* F, jint index) const
	{
		return *(T) ptr(F, index, sizeof(T));
	}
};

}}}

#endif
