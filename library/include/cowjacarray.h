#ifndef COWJACARRAY_H
#define COWJACARRAY_H

#include "java.lang.Object.h"

namespace com {
namespace cowlark {
namespace cowjac {

class BaseArray : public java::lang::Object
{
public:
	BaseArray(jint size, unsigned elementLength);
	virtual ~BaseArray();
};

template <class T> class Array : public BaseArray
{
public:
	Array(jint size): BaseArray(size, sizeof(T))
	{}
};

}}}

#endif
