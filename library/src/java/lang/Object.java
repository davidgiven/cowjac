package java.lang;

public class Object
{
	public native int hashCode();
	public native boolean equals(Object o);
	
	public String toString()
	{
		return "[" + hashCode() + "]";
	}
}
