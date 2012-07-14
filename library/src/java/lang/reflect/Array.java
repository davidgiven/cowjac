package java.lang.reflect;

import com.cowlark.cowjac.harmony.Native;

public class Array
{
	@Native("newInstance")
	public static native Object newInstance(Class<?> componentType, int size);
}
