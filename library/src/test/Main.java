package test;

import com.cowlark.cowjac.harmony.Native;

public class Main
{
	public int manyparams(int i1, int i2, int i3, int i4, int i5, int i6)
	{
		if (i1 < 0)
			return 42;
		else
			return i1+i2+i3+i4+i5+i6;
	}
	
	private String s = "Hello, world";
	
	public void test(Main m)
	{
		s = s + "fnord";
	}
	
	public Main test1()
	{
		return (Main)(Object) Main.class;
	}
	
	@Native("main")
	public static final void main(String[] argv)
	{
		Main m = new Main();
		m.test(m);
		m.test(null);
	}
}
