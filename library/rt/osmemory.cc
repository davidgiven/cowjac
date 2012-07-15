/* cowjac © 2012 David Given
 * This file is licensed under the Simplified BSD license. Please see
 * COPYING.cowjac for the full text.
 */

#include <stdlib.h>
#include <assert.h>
#include "cowjac.h"
#include "cowjacarray.h"
#include "cowjacclass.h"
#include "java.lang.Float.h"
#include "java.lang.Double.h"
#include "java.io.IOException.h"
#include "org.apache.harmony.luni.platform.OSMemory.h"

using com::cowlark::cowjac::Stackframe;
using com::cowlark::cowjac::NullCheck;
using com::cowlark::cowjac::ScalarArray;

jfloat java::lang::Float::m_intBitsToFloat_5f_28I_29F
		(Stackframe* F, jint bits)
{
	return *(jfloat*)&bits;
}

jdouble java::lang::Double::m_longBitsToDouble_5f_28J_29D
		(Stackframe* F, jlong bits)
{
	return *(jdouble*)&bits;
}

jboolean org::apache::harmony::luni::platform::OSMemory::isLittleEndianImpl
		(Stackframe* F)
{
	int i = 1;
	return (*(jbyte*)&i) == 1;
}

jint org::apache::harmony::luni::platform::OSMemory::getPointerSizeImpl
		(Stackframe* F)
{
	return sizeof(void*);
}

jlong org::apache::harmony::luni::platform::OSMemory::m_malloc_5f_28J_29J
		(Stackframe* F, jlong length)
{
	return (jlong) malloc(length);
}

void org::apache::harmony::luni::platform::OSMemory::m_free_5f_28J_29V
		(Stackframe* F, jlong ptr)
{
	free((void*) ptr);
}

void org::apache::harmony::luni::platform::OSMemory::m_memset_5f_28JBJ_29V
		(Stackframe* F, jlong address, jbyte value, jlong length)
{
	memset((void*)address, value, length);
}

void org::apache::harmony::luni::platform::OSMemory::m_memmove_5f_28JJJ_29V
		(Stackframe* F, jlong destAddress, jlong srcAddress, jlong length)
{
	memmove((void*)destAddress, (void*)srcAddress, length);
}

jbyte org::apache::harmony::luni::platform::OSMemory::m_getByte_5f_28J_29B
		(Stackframe* F, jlong address)
{
	return *(jbyte*)address;
}

void org::apache::harmony::luni::platform::OSMemory::m_setByte_5f_28JB_29V
		(Stackframe* F, jlong address, jbyte value)
{
	*(jbyte*)address = value;
}

jshort org::apache::harmony::luni::platform::OSMemory::m_getShort_5f_28J_29S
		(Stackframe* F, jlong address)
{
	return *(jshort*)address;
}

void org::apache::harmony::luni::platform::OSMemory::m_setShort_5f_28JS_29V
		(Stackframe* F, jlong address, jshort value)
{
	*(jshort*)address = value;
}

jint org::apache::harmony::luni::platform::OSMemory::m_getInt_5f_28J_29I
		(Stackframe* F, jlong address)
{
	return *(jint*)address;
}

void org::apache::harmony::luni::platform::OSMemory::m_setInt_5f_28JI_29V
		(Stackframe* F, jlong address, jint value)
{
	*(jint*)address = value;
}

jlong org::apache::harmony::luni::platform::OSMemory::m_getLong_5f_28J_29J
		(Stackframe* F, jlong address)
{
	return *(jlong*)address;
}

void org::apache::harmony::luni::platform::OSMemory::m_setLong_5f_28JJ_29V
		(Stackframe* F, jlong address, jlong value)
{
	*(jlong*)address = value;
}

jfloat org::apache::harmony::luni::platform::OSMemory::m_getFloat_5f_28J_29F
		(Stackframe* F, jlong address)
{
	return *(jfloat*)address;
}

void org::apache::harmony::luni::platform::OSMemory::m_setFloat_5f_28JF_29V
		(Stackframe* F, jlong address, jfloat value)
{
	*(jfloat*)address = value;
}

jdouble org::apache::harmony::luni::platform::OSMemory::m_getDouble_5f_28J_29D
		(Stackframe* F, jlong address)
{
	return *(jdouble*)address;
}

void org::apache::harmony::luni::platform::OSMemory::m_setDouble_5f_28JD_29V
		(Stackframe* F, jlong address, jdouble value)
{
	*(jdouble*)address = value;
}

jlong org::apache::harmony::luni::platform::OSMemory::m_getAddress_5f_28J_29J
		(Stackframe* F, jlong address)
{
	return *(intptr_t*)address;
}

void org::apache::harmony::luni::platform::OSMemory::m_setAddress_5f_28JJ_29V
		(Stackframe* F, jlong address, jlong value)
{
	*(intptr_t*)address = value;
}

void org::apache::harmony::luni::platform::OSMemory::m_getByteArray_5f_28J_5bBII_29V
		(Stackframe* F, jlong address, ScalarArray<jbyte>* array, jint base, jint length)
{
	jbyte* srcptr = NullCheck((jbyte*)address);
	array->boundsCheck(F, base);
	array->boundsCheck(F, base+length-1);
	jbyte* destptr = (jbyte*) array->ptr(base);
	memcpy(destptr, srcptr, length);
}

void org::apache::harmony::luni::platform::OSMemory::m_setByteArray_5f_28J_5bBII_29V
		(Stackframe* F, jlong address, ScalarArray<jbyte>* array, jint base, jint length)
{
	jbyte* destptr = NullCheck((jbyte*)address);
	array->boundsCheck(F, base);
	array->boundsCheck(F, base+length-1);
	jbyte* srcptr = (jbyte*) array->ptr(base);
	memcpy(destptr, srcptr, length);
}

/* The mmap stuff is currently unimplemented. */

jlong org::apache::harmony::luni::platform::OSMemory::m_mmapImpl_5f_28JJJI_29J
		(Stackframe* F, jlong fileDescriptor, jlong alignment,
				jlong size, jint mapMode)
{
	java::io::IOException* e = new java::io::IOException();
	e->m__3cinit_3e_5f_28_29V(F);
	throw e;
}

void org::apache::harmony::luni::platform::OSMemory::m_unmapImpl_5f_28JJ_29V
		(Stackframe* F, jlong address, jlong size)
{
}

jboolean org::apache::harmony::luni::platform::OSMemory::m_isLoadedImpl_5f_28JJ_29Z
		(Stackframe* F, jlong address, jlong size)
{
	return false;
}

jint org::apache::harmony::luni::platform::OSMemory::m_loadImpl_5f_28JJ_29I
		(Stackframe* F, jlong address, jlong size)
{
	return 0;
}

int org::apache::harmony::luni::platform::OSMemory::m_flushImpl_5f_28JJ_29I
		(Stackframe* F, jlong address, jlong size)
{
	return -1;
}
