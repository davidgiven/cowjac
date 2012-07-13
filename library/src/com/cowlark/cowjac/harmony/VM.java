package com.cowlark.cowjac.harmony;

import java.util.HashMap;

public class VM
{
	private static HashMap<String, String> _internSet = new HashMap<String, String>();
	
	public static synchronized String intern(String s)
	{
		String si = _internSet.get(s);
		if (si != null)
			return si;
		
		_internSet.put(s, s);
		return s;
	}
}
