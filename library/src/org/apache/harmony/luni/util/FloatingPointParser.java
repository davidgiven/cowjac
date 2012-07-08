package org.apache.harmony.luni.util;

public class FloatingPointParser
{
	public static float parseFloat(String s)
	{
		return parseDouble(s);
	}
	
	public static native float parseDouble(String s);
}
