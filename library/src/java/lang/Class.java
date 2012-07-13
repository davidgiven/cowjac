package java.lang;

import com.cowlark.cowjac.harmony.Native;

public abstract class Class<T extends Object>
{
	@Native("isPrimitive")
	public boolean isPrimitive() { return false; }
	
	@Native("isArray")
	public boolean isArray() { return false; }
	
	@Native("getSuperclass")
	public Class<?> getSuperclass() { return null; }
	
	@Native("getComponentType")
	public Class<?> getComponentType() { return null; }
	
	@Native("getName")
	public String getName() { return "class"; }
	
	public final boolean desiredAssertionStatus()
	{
		return true;
	}
}
