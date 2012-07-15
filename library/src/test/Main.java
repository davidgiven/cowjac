/* cowjac © 2012 David Given
 * This file is licensed under the Simplified BSD license. Please see
 * COPYING.cowjac for the full text.
 */

package test;

import java.io.IOException;
import com.cowlark.cowjac.harmony.Native;

public class Main
{
	@Native("main")
	public static final void main(String[] argv) throws IOException
	{
		System.out.println("Hello, world!");
	}
}
