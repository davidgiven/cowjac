#include <stdlib.h>
#include <stdio.h>
#include <assert.h>
#include <unistd.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <sys/ioctl.h>
#include "cowjac.h"
#include "cowjacarray.h"
#include "cowjacclass.h"
#include "java.io.IOException.h"
#include "java.io.File.h"
#include "java.io.FileDescriptor.h"
#include "org.apache.harmony.luni.platform.OSFileSystem.h"

using com::cowlark::cowjac::Stackframe;
using com::cowlark::cowjac::NullCheck;
using com::cowlark::cowjac::ScalarArray;
using com::cowlark::cowjac::ObjectArray;

void java::io::File::m_oneTimeInitialization_5f_28_29V
		(Stackframe* F)
{
}

void java::io::FileDescriptor::m_oneTimeInitialization_5f_28_29V
		(Stackframe* F)
{
}

void org::apache::harmony::luni::platform::OSFileSystem::m_oneTimeInitializationImpl_5f_28_29V
		(Stackframe* F)
{
}

jboolean java::io::File::m_isCaseSensitiveImpl_5f_28_29Z
		(Stackframe* F)
{
	return true;
}

jlong java::io::FileDescriptor::m_getStdInDescriptor_5f_28_29J
		(Stackframe* F)
{
	return 0;
}

jlong java::io::FileDescriptor::m_getStdOutDescriptor_5f_28_29J
		(Stackframe* F)
{
	return 1;
}

jlong java::io::FileDescriptor::m_getStdErrDescriptor_5f_28_29J
		(Stackframe* F)
{
	return 2;
}

jint org::apache::harmony::luni::platform::OSFileSystem::m_getAllocGranularity_5f_28_29I
		(Stackframe* F)
{
	return 4096;
}

ScalarArray<jbyte>* java::io::File::m_getCanonImpl_5f_28_5bB_29_5bB
		(Stackframe* F, ScalarArray<jbyte>* filename)
{
	return filename;
}

ScalarArray<jbyte>* java::io::File::m_getLinkImpl_5f_28_5bB_29_5bB
		(Stackframe* F, ScalarArray<jbyte>* filename)
{
	assert(false);
	return 0;
}

jboolean java::io::File::m_isHiddenImpl_5f_28_5bB_29Z
		(Stackframe* F, ScalarArray<jbyte>* filename)
{
	NullCheck(filename);
	return (filename->length() > 0) && (filename->get(F, 0) == '.');
}


jboolean java::io::File::m_isDirectoryImpl_5f_28_5bB_29Z
		(Stackframe* F, ScalarArray<jbyte>* filename)
{
	NullCheck(filename);
	char filenamez[filename->length() + 1];
	memcpy(filenamez, filename->ptr(0), filename->length());

	struct stat st;
	int i = stat(filenamez, &st);
	return (i == 0) && S_ISDIR(st.st_mode);
}

jboolean java::io::File::m_isFileImpl_5f_28_5bB_29Z
		(Stackframe* F, ScalarArray<jbyte>* filename)
{
	NullCheck(filename);
	char filenamez[filename->length() + 1];
	memcpy(filenamez, filename->ptr(0), filename->length());

	struct stat st;
	int i = stat(filenamez, &st);
	return (i == 0) && S_ISREG(st.st_mode);
}

jlong java::io::File::m_lastModifiedImpl_5f_28_5bB_29J
		(Stackframe* F, ScalarArray<jbyte>* filename)
{
	NullCheck(filename);
	char filenamez[filename->length() + 1];
	memcpy(filenamez, filename->ptr(0), filename->length());

	struct stat st;
	int i = stat(filenamez, &st);
	return (jlong)st.st_mtime * 1000LL;
}

jboolean java::io::File::m_setLastModifiedImpl_5f_28_5bBJ_29Z
		(Stackframe* F, ScalarArray<jbyte>* filename, jlong mtime)
{
	assert(false);
	return false;
}

jboolean java::io::File::m_isReadOnlyImpl_5f_28_5bB_29Z
		(Stackframe* F, ScalarArray<jbyte>* filename)
{
	NullCheck(filename);
	char filenamez[filename->length() + 1];
	memcpy(filenamez, filename->ptr(0), filename->length());

	return access(filenamez, W_OK) == -1;
}

jboolean java::io::File::m_setReadOnlyImpl_5f_28_5bB_29Z
		(Stackframe* F, ScalarArray<jbyte>* filename)
{
	assert(false);
	return false;
}

jboolean java::io::File::m_isWriteOnlyImpl_5f_28_5bB_29Z
		(Stackframe* F, ScalarArray<jbyte>* filename)
{
	NullCheck(filename);
	char filenamez[filename->length() + 1];
	memcpy(filenamez, filename->ptr(0), filename->length());

	return access(filenamez, R_OK) == -1;
}

jboolean java::io::File::m_existsImpl_5f_28_5bB_29Z
		(Stackframe* F, ScalarArray<jbyte>* filename)
{
	NullCheck(filename);
	char filenamez[filename->length() + 1];
	memcpy(filenamez, filename->ptr(0), filename->length());

	return access(filenamez, F_OK) == 0;
}

jlong java::io::File::m_lengthImpl_5f_28_5bB_29J
		(Stackframe* F, ScalarArray<jbyte>* filename)
{
	NullCheck(filename);
	char filenamez[filename->length() + 1];
	memcpy(filenamez, filename->ptr(0), filename->length());

	struct stat st;
	int i = stat(filenamez, &st);
	return st.st_size;
}


jboolean java::io::File::m_deleteFileImpl_5f_28_5bB_29Z
		(Stackframe* F, ScalarArray<jbyte>* filename)
{
	NullCheck(filename);
	char filenamez[filename->length() + 1];
	memcpy(filenamez, filename->ptr(0), filename->length());

	return unlink(filenamez) == 0;
}

jboolean java::io::File::m_deleteDirImpl_5f_28_5bB_29Z
		(Stackframe* F, ScalarArray<jbyte>* filename)
{
	NullCheck(filename);
	char filenamez[filename->length() + 1];
	memcpy(filenamez, filename->ptr(0), filename->length());

	return rmdir(filenamez) == 0;
}

jboolean java::io::File::m_mkdirImpl_5f_28_5bB_29Z
		(Stackframe* F, ScalarArray<jbyte>* filename)
{
	NullCheck(filename);
	char filenamez[filename->length() + 1];
	memcpy(filenamez, filename->ptr(0), filename->length());

	return mkdir(filenamez, 0777) == 0;
}

jboolean java::io::File::m_renameToImpl_5f_28_5bB_5bB_29Z
		(Stackframe* F, ScalarArray<jbyte>* src, ScalarArray<jbyte>* dest)
{
	NullCheck(src);
	char srcz[src->length() + 1];
	memcpy(srcz, src->ptr(0), src->length());

	NullCheck(dest);
	char destz[dest->length() + 1];
	memcpy(destz, dest->ptr(0), dest->length());

	return rename(srcz, destz) == 0;
}

jint java::io::File::m_newFileImpl_5f_28_5bB_29I
		(Stackframe* F, ScalarArray<jbyte>* filename)
{
	NullCheck(filename);
	char filenamez[filename->length() + 1];
	memcpy(filenamez, filename->ptr(0), filename->length());

	int fd = open(filenamez, O_CREAT|O_WRONLY, 0644);
	if (fd != -1)
		close(fd);
	return (fd != -1) ? 0 : -1;
}

ObjectArray* java::io::File::m_listImpl_5f_28_5bB_29_5b_5bB
		(Stackframe* F, ScalarArray<jbyte>* path)
{
	assert(false);
	return 0;
}

void java::io::FileDescriptor::m_syncImpl_5f_28_29V
		(Stackframe* F)
{
	if (descriptor != -1)
		fsync(descriptor);
}

jlong org::apache::harmony::luni::platform::OSFileSystem::m_openImpl_5f_28_5bBI_29J
		(Stackframe* F, ScalarArray<jbyte>* filename, int jmode)
{
	NullCheck(filename);
	char filenamez[filename->length() + 1];
	memcpy(filenamez, filename->ptr(0), filename->length());

	/* From IFileSystem.java */
	const int J_RDONLY = 0x00000000;
	const int J_WRONLY = 0x00000001;
	const int J_RDWR = 0x00000010;
	const int J_RDWRSYNC = 0x00000020;
	const int J_APPEND = 0x00000100;
	const int J_CREAT = 0x00001000;
	const int J_EXCL = 0x00010000;
	const int J_NOCTTY = 0x00100000;
	const int J_NONBLOCK = 0x01000000;
	const int J_TRUNC = 0x10000000;

	int mode = 0;
	int i = jmode & 3;
	if (i == J_RDONLY)
		mode = O_RDONLY;
	else if (i == J_WRONLY)
		mode = O_WRONLY;
	else if (i == J_RDWR)
		mode = O_RDWR;

	if (jmode & J_APPEND)
		mode |= O_APPEND;
	if (jmode & J_CREAT)
		mode |= O_CREAT;
	if (jmode & J_NONBLOCK)
		mode |= O_NONBLOCK;
	if (jmode & J_TRUNC)
		mode |= O_TRUNC;

	return open(filenamez, mode);
}

jlong org::apache::harmony::luni::platform::OSFileSystem::m_ttyReadImpl_5f_28_5bBII_29J
		(Stackframe* F, ScalarArray<jbyte>* buffer, jint offset, jint length)
{
	NullCheck(buffer);
	buffer->boundsCheck(F, offset);
	buffer->boundsCheck(F, offset+length-1);
	jbyte* p = (jbyte*) buffer->ptr(offset);

	return read(0, p, length);
}

jlong org::apache::harmony::luni::platform::OSFileSystem::m_ttyAvailableImpl_5f_28_29J
		(Stackframe* F)
{
	int i = 0;
	ioctl(0, FIONREAD, &i);
	return i;
}

jint org::apache::harmony::luni::platform::OSFileSystem::m_lockImpl_5f_28JJJIZ_29I
		(Stackframe*, jlong, jlong, jlong, jint, jboolean)
{
	assert(false);
	return -1;
}

jint org::apache::harmony::luni::platform::OSFileSystem::m_unlockImpl_5f_28JJJ_29I
		(Stackframe*, jlong, jlong, jlong)
{
	assert(false);
	return -1;
}

jint org::apache::harmony::luni::platform::OSFileSystem::m_fflushImpl_5f_28JZ_29I
		(Stackframe*, jlong fd, jboolean metadata)
{
	if (metadata)
		return fsync(fd) == 0;
	else
		return fdatasync(fd) == 0;
}

jlong org::apache::harmony::luni::platform::OSFileSystem::m_seekImpl_5f_28JJI_29J
		(Stackframe*, jlong fd, jlong offset, jint whence)
{
	return lseek(fd, offset, whence);
}

jlong org::apache::harmony::luni::platform::OSFileSystem::m_readDirectImpl_5f_28JJII_29J
		(Stackframe*, jlong fd, jlong address, jint offset, jint length)
{
	return read(fd, (jbyte*)address + offset, length);
}

jlong org::apache::harmony::luni::platform::OSFileSystem::m_writeDirectImpl_5f_28JJII_29J
		(Stackframe*, jlong fd, jlong address, jint offset, jint length)
{
	return write(fd, (jbyte*)address + offset, length);
}

jlong org::apache::harmony::luni::platform::OSFileSystem::m_readImpl_5f_28J_5bBII_29J
		(Stackframe* F, jlong fd, ScalarArray<jbyte>* array, jint offset, jint length)
{
	NullCheck(array);
	array->boundsCheck(F, offset);
	array->boundsCheck(F, offset+length-1);
	return read(fd, array->ptr(offset), length);
}

jlong org::apache::harmony::luni::platform::OSFileSystem::m_writeImpl_5f_28J_5bBII_29J
		(Stackframe* F, jlong fd, ScalarArray<jbyte>* array, jint offset, jint length)
{
	NullCheck(array);
	array->boundsCheck(F, offset);
	array->boundsCheck(F, offset+length-1);
	return write(fd, array->ptr(offset), length);
}

jint org::apache::harmony::luni::platform::OSFileSystem::m_truncateImpl_5f_28JJ_29I
		(Stackframe* F, jlong fd, jlong length)
{
	return ftruncate(fd, length);
}

jint org::apache::harmony::luni::platform::OSFileSystem::m_closeImpl_5f_28J_29I
		(Stackframe* F, jlong fd)
{
	return close(fd);
}

jlong org::apache::harmony::luni::platform::OSFileSystem::m_sizeImpl_5f_28J_29J
		(Stackframe* F, jlong fd)
{
	struct stat st;
	int i = fstat(fd, &st);
	if (i == -1)
		return -1;
	return st.st_size;
}

jlong org::apache::harmony::luni::platform::OSFileSystem::m_availableImpl_5f_28J_29J
		(Stackframe* F, jlong fd)
{
	int i = 1;
	ioctl(fd, FIONREAD, &i);
	return i;
}
