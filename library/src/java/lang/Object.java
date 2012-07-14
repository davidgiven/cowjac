package java.lang;

import java.io.IOException;
import com.cowlark.cowjac.harmony.Native;

public class Object
{
	@Native("hashCode")
	public native int hashCode();
	
	@Native("getClass")
	public native Class<? extends Object> getClass();
	
    @Native("equals")
    public native boolean equals(Object object);
    
	public String toString()
	{
		return "[" + hashCode() + "]";
	}
	
    protected Object clone() throws CloneNotSupportedException
	{
		throw new CloneNotSupportedException();
	}
    
}
