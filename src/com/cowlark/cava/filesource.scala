package com.cowlark.cava
import java.io.File
import java.util.jar.JarFile
import java.util.Enumeration
import java.io.InputStream
import java.io.FileInputStream

abstract class FileSource
{
	def foreach(cb: (InputStream) => Unit)
}

class JarFileSource(jarfile: String) extends FileSource
{
	val jar = new JarFile(jarfile)
	
	override def foreach(cb: (InputStream) => Unit)
	{
		def enumerationToIterator[A](e : Enumeration[A]) =
			new Iterator[A]
			{
				def next = e.nextElement
				def hasNext = e.hasMoreElements
			}
		
		for (entry <- enumerationToIterator(jar.entries))
		{
			if (entry.getName.endsWith(".class"))
			{
				var is = jar.getInputStream(entry)
				cb(is)
				is.close
			}
		}
	}
}

class DirectoryFileSource(dirname: String) extends FileSource
{
	override def foreach(cb: (InputStream) => Unit)
	{
		def traverse(dir: File)
		{
			var children = dir.listFiles
			for (f <- children)
			{
				if (f.isDirectory)
					traverse(f)
				else if (f.getName.endsWith(".class"))
				{
					var fis = new FileInputStream(f) 
					cb(fis)
					fis.close
				}
			}
		}
	
		traverse(new File(dirname))
	}
}
