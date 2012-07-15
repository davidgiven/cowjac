/* cowjac © 2012 David Given
 * This file is licensed under the Simplified BSD license. Please see
 * COPYING.cowjac for the full text.
 */

#include <time.h>
#include "cowjac.h"
#include "cowjacarray.h"
#include "cowjacclass.h"
#include "java.lang.System.h"

using com::cowlark::cowjac::SystemLock;
using com::cowlark::cowjac::Stackframe;

jlong java::lang::System::currentTimeMillis
		(Stackframe* F)
{
	struct timespec ts;
	clock_gettime(CLOCK_REALTIME, &ts);
	return ((jlong)ts.tv_sec * 1000LL) +
			((jlong)ts.tv_nsec / 1000000LL);
}

