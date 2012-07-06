package test;

public abstract class Main
{
	private int i_private;
	public long i_public;
	protected double i_protected;
	char i_default;
	static Main i_static;
	
	private void m_private() {}
	public void m_public() {}
	protected void m_protected() {}
	void m_default() {}
	static void m_static() {}
	public abstract void m_abstract();
	
	public int manyparams(int i1, int i2, int i3, int i4, int i5, int i6)
	{
		return i1+i2+i3+i4+i5+i6;
	}
	
	public static final void main(String[] argv)
	{
	}
}
