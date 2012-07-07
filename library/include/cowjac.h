#ifndef COWJAC_H
#define COWJAC_H

#include <stdlib.h>
#include <stdint.h>
#include <list>
typedef bool jboolean;
typedef uint8_t jbyte;
typedef int16_t jshort;
typedef uint16_t jchar;
typedef int32_t jint;
typedef int64_t jlong;
typedef float jfloat;
typedef double jdouble;

namespace com {
namespace cowlark {
namespace cowjac {

/* Indicates that an object contains object references. */

class ContainsReferences
{
public:
	virtual void ___mark() = 0;
};

/* All Java classes inherit from this. */

class Object : public ContainsReferences
{
public:
	Object();

	void ___mark() {}
};

/* A stack frame. */

class Stackframe : public ContainsReferences
{
public:
	Stackframe(Stackframe* parent):
		_parent(parent),
		_next(NULL)
	{
		parent->_next = this;
	}

	~Stackframe()
	{
		_parent->_next = NULL;
	}

private:
	Stackframe* _parent;
	Stackframe* _next;
};

/* An object reference root. */

class BaseGlobalReference : public ContainsReferences
{
public:
	BaseGlobalReference();
	~BaseGlobalReference();

	void ___mark();

protected:
	Object* _data;
};

template <class T> class GlobalReference : public BaseGlobalReference
{
public:
	GlobalReference(): BaseGlobalReference() {}
	~GlobalReference() {}

	template <class S> GlobalReference<T>& operator = (S val)
	{
		_data = val;
		return *this;
	}

	operator T () const
	{
		return (T) _data;
	}

	T operator -> () const
	{
		return (T) _data;
	}
};

}}}

#endif
