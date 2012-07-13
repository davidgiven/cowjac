package org.apache.harmony.luni.util;

import com.cowlark.cowjac.harmony.Native;

public class FloatingPointParser
{
	public static float parseFloat(String s)
	{
		return (float) parseDouble(s);
	}
	
	@Native("parseDouble")
	public static native double parseDouble(String s);
}
