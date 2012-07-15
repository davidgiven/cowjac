/* cowjac © 2012 David Given
 * This file is licensed under the Simplified BSD license. Please see
 * COPYING.cowjac for the full text.
 */

package com.cowlark.cowjac

trait Utils
{
	private def mangleFilenameImpl(name: String): String =
	{
		val sb = new StringBuilder()
		
		for (c <- name)
		{
			if (c == '_')
				sb ++= "__"
			else if (c == '$')
				sb ++= "_S"
			else
				sb += c
		}
		
		return sb.toString
	}
	
	var mangleFilename = Memoize(mangleFilenameImpl)
}