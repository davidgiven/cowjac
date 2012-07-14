package java.lang;

import com.cowlark.cowjac.harmony.Native;

public class System
{
	public static String getProperty(String key)
	{
		return "";
	}
	
	public static String getProperty(String key, String defaultValue)
	{
		return defaultValue;
	}

	public static void arraycopy(Object src, int srcPos, Object dest, int destPos,
            int length)
	{
		throw new UnsupportedOperationException();
	}

	@Native("currentTimeMillis")
	public static native long currentTimeMillis();
	
}
