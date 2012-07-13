#ifndef COWJAC_H
#define COWJAC_H

#include <stdlib.h>
#include <string.h>
#include <stdint.h>
#include <math.h>
typedef bool jboolean;
typedef uint8_t jbyte;
typedef int16_t jshort;
typedef uint16_t jchar;
typedef int32_t jint;
typedef uint32_t juint;
typedef int64_t jlong;
typedef uint64_t julong;
typedef float jfloat;
typedef double jdouble;

namespace com {
namespace cowlark {
namespace cowjac {

/* An RAII global system lock. */

class SystemLock
{
public:
	SystemLock();
	~SystemLock();
};

/* Indicates that an object contains object references. */

class ContainsReferences
{
public:
	virtual void mark() = 0;
};

/* All Java classes inherit from this. */

class Object : public ContainsReferences
{
public:
	Object();

	void mark() {}
	virtual void markImpl();

	void enterMonitor();
	void leaveMonitor();

private:
	bool _marked: 1;
	bool _immutable: 1;
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

	void mark() {}

protected:
	void markMany(ContainsReferences** ptr, unsigned int count);

private:
	Stackframe* _parent;
	Stackframe* _next;
};

/* An entity which adds itself to the global root list. */

class ContainsGlobalReferences : public ContainsReferences
{
public:
	ContainsGlobalReferences();
	~ContainsGlobalReferences();

	static ContainsGlobalReferences* getFirstReference()
	{ return _first; }

private:
	ContainsGlobalReferences* _next;
	ContainsGlobalReferences* _prev;
	static ContainsGlobalReferences* _first;
};

/* Check for and throw a NullPointerException. */

extern void throwNullPointerException();

template <class T> T NullCheck(T t)
{
	if (!t)
		throwNullPointerException();
	return t;
}

/* Cast one type to another, throwing a ClassCastException if needed. */

extern __attribute__((noreturn)) void CastFailed(Stackframe* f);
template <class SRC, class DEST> DEST Cast(Stackframe* f, SRC src)
{
	DEST dest = dynamic_cast<DEST>(src);
	if (!dest)
		CastFailed(f);
	return dest;
}

/* Unsigned shift operations. */

jint Ushr(jint value, jint shift)
{
	return (jint) (((juint)value) >> shift);
}

jlong Ushr(jlong value, jint shift)
{
	return (jlong) (((julong)value) >> shift);
}

int Cmp(jlong a, jlong b)
{
	if (a == b)
		return 0;
	if (a > b)
		return 1;
	else
		return -1;
}

int Cmpl(double a, double b)
{
	if (a > b)
		return 1;
	if (a < b)
		return -1;
	if (a == b)
		return 0;
	return -1;
}

int Cmpg(double a, double b)
{
	if (a > b)
		return 1;
	if (a < b)
		return -1;
	if (a == b)
		return 0;
	return 1;
}

}}}

#endif
