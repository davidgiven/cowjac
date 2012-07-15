/* cowjac © 2012 David Given
 * This file is licensed under the Simplified BSD license. Please see
 * COPYING.cowjac for the full text.
 */

#include <stdlib.h>
#include <assert.h>
#include "cowjac.h"
#include "cowjacarray.h"
#include "cowjacclass.h"

void* com::cowlark::cowjac::Object::operator new (size_t size)
{
	void* ptr = malloc(size);
	memset(ptr, 0, size);
	return ptr;
}

void com::cowlark::cowjac::Object::operator delete (void* ptr)
{
	free(ptr);
}
