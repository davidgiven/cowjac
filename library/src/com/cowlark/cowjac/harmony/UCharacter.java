package com.cowlark.cowjac.harmony;

import java.util.Locale;

public class UCharacter
{
	public static String toLowerCase(Locale locale, String s)
	{
		char[] chars = s.toCharArray();
		for (int i = 0; i < chars.length; i++)
			chars[i] = Character.toLowerCase(chars[i]);
		return new String(chars);
	}
}
