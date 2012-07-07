#ifndef COWJAC_H
#define COWJAC_H

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

class ContainsReferences;
class BaseGlobalReference;

/* The per-thread runtime context. */

class Context
{
public:
	static Context* getCurrentContext();

public:
	BaseGlobalReference* _firstReference;
};

/* Indicates that an object contains object references. */

class ContainsReferences
{
public:
	virtual void __gc(int mode);
};

/* All Java classes inherit from this. */

class Object : public ContainsReferences
{
public:
	Object();
};

/* An object reference root. */

class BaseGlobalReference : public ContainsReferences
{
public:
	BaseGlobalReference(Context* context):
			_context(context),
			_next(context->_firstReference),
			_prev(0)
	{
		context->_firstReference = this;
	}

	~BaseGlobalReference()
	{
		if (_context->_firstReference == this)
			_context->_firstReference = _next;

		if (_prev)
			_prev->_next = _next;
		if (_next)
			_next->_prev = _prev;
	}

private:
	BaseGlobalReference* _next;
	BaseGlobalReference* _prev;
	Context* _context;

protected:
	Object* _data;
};

template <class T> class GlobalReference : public BaseGlobalReference
{
public:
	GlobalReference(Context* context): BaseGlobalReference(context) {}
	GlobalReference(): BaseGlobalReference(Context::getCurrentContext()) {}
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
};

}}}

#endif
