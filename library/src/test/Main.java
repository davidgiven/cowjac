package test;

import java.util.HashMap;
import java.util.Map;
import com.cowlark.cowjac.harmony.Native;

public class Main
{
	@Native("main")
	public static final void main(String[] argv)
	{
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		
		for (int i = 0; i < 100; i++)
			map.put(i, -i);
		
		int i = map.get(42);
	}
}
