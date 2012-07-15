package test;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import com.cowlark.cowjac.harmony.Native;

public class Main
{
	public static boolean test(float f1, float f2)
	{
		return (f1 < f2);
	}
	
	@Native("main")
	public static final void main(String[] argv)
	{
		FileDescriptor fd = FileDescriptor.out;
		FileOutputStream fos = new FileOutputStream(fd);
		PrintStream ps = new PrintStream(fos);
		
		ps.print("Hello, world!\n");
		ps.flush();
	}
}
