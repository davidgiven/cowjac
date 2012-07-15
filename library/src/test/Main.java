package test;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import com.cowlark.cowjac.harmony.Native;

public class Main
{
	@Native("main")
	public static final void main(String[] argv)
	{
		String s = new String(new char[] { 'H', 'e', 'l', 'l', 'o', ' ', 'w', 'o', 'r', 'l', 'd', '\n' });
		
		FileDescriptor fd = FileDescriptor.out;
		FileOutputStream fos = new FileOutputStream(fd);
		
	}
}
