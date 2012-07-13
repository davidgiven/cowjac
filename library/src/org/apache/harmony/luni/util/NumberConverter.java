package org.apache.harmony.luni.util;

import com.cowlark.cowjac.harmony.Native;

public class NumberConverter
{
	@Native("convert")
	public static native String convert(double d);
}
