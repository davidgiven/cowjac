package test;

import com.cowlark.cowjac.harmony.Native;

public class Main
{
	private static class S1
	{
		public static int i = 1;
	}
	
	private static class S2 extends S1
	{
		public static int j = 2;
	}
	
	@Native("main")
	public static final void main(String[] argv)
	{
		S2 s2 = new S2();
	}
}
