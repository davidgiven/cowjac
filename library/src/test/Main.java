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
	
	public static final void main(String[] argv)
	{
	}
}
