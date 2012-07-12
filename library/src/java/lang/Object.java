package java.lang;

import com.cowlark.cowjac.harmony.Native;

public class Object
{
	public native int hashCode();
	public native Class<? extends Object> getClass();
	
	public String toString()
	{
		return "[" + hashCode() + "]";
	}
	
    protected Object clone() throws CloneNotSupportedException
	{
		throw new CloneNotSupportedException();
	}
    
    @Native("equals")
    public native boolean equals(Object object);
}
