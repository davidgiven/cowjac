package java.lang;

public class Object
{
	public native int hashCode();
	public native boolean equals(Object o);
	public native Class<? extends Object> getClass();
	
	public String toString()
	{
		return "[" + hashCode() + "]";
	}
	
    protected Object clone() throws CloneNotSupportedException
	{
		throw new CloneNotSupportedException();
	}
}
