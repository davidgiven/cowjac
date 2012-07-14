package org.apache.harmony.luni.platform;

public class NoopMemorySpy implements IMemorySpy
{
    public void alloc(PlatformAddress address)
    {
    }

    public boolean free(PlatformAddress address)
    {
    	return true;
    }

    public void rangeCheck(PlatformAddress address, int offset, int length)
            throws IndexOutOfBoundsException
    {
    }

    public void autoFree(PlatformAddress address)
    {
    }
}
