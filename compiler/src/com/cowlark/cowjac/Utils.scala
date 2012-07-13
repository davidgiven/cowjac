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