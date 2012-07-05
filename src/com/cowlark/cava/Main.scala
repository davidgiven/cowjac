package com.cowlark.cava
import scala.collection.immutable.HashMap
import soot.Scene

object Main
{
	private var inputjar: String = null
	private var inputdir: String = null
	
	private val arguments: Array[ArgumentParser.Descriptor] = Array(
			("h", "help",     false, (s: String) => help),
			("j", "inputjar", true,  (s: String) => inputjar = s),
			("d", "inputdir", true,  (s: String) => inputdir = s)
		)
	
	private def help
	{
		System.out.println("No help yet.")
		System.exit(0)
	}
	
	private def error(message: String, args: AnyRef*)
	{
		System.err.println("Error: " + message.format(args))
		System.exit(1)
	}
	
	def main(argv: Array[String])
	{
		val parser = new ArgumentParser(arguments)
		val inputfiles = parser.wrappedProcess(argv, error(_, _))
		
		if ((inputjar == null) && (inputdir == null))
			error("You must specify an input jar or directory.")
		if ((inputjar != null) && (inputdir != null))
			error("You cannot specify both an input jar and a directory.")
		if (!inputfiles.isEmpty)
			error("Extraneous files on command line (try --help)")
		
		val filereader: FileSource =
			if (inputjar != null) new JarFileSource(inputjar)
			else if (inputdir != null) new DirectoryFileSource(inputdir)
			else null
		
		for (f <- filereader)
			System.out.println("Process file "+f)
	}
}
