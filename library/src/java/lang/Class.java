package java.lang;

public class Class<T extends Object>
{
	public native boolean isPrimitive();
	public native boolean isArray();
	public native Class<?> getSuperclass();
	public native Class<?> getComponentType();
	public native String getName();
	
	public final boolean desiredAssertionStatus()
	{
		return true;
	}
}
