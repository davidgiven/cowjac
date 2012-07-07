package com.cowlark.cowjac
import scala.collection.immutable.HashMap
import soot.Scene
import soot.CompilationDeathException
import soot.PackManager
import soot.util.Chain
import soot.options.Options
import scala.collection.JavaConversions._

object Main
{
	private var inputjar: String = null
	private var inputdir: String = null
	private var outputdir: String = null
	private var mainclassname: String = null
	
	private val argumentParser = new ArgumentParser(
			Array(
				ArgumentParser.Descriptor("h", "help", false,
						"Report usage information",
						(s: String) => help),
				ArgumentParser.Descriptor("j", "inputjar", true,
						"Read input classes from jarfile",
						(s: String) => inputjar = s),
				ArgumentParser.Descriptor("d", "inputdir", true,
						"Read input classes from directory",
						(s: String) => inputdir = s),
				ArgumentParser.Descriptor("o", "outputdir", true,
						"Write output to directory",
						(s: String) => outputdir = s),
				ArgumentParser.Descriptor("m", "main", true,
						"Specify main class",
						(s: String) => mainclassname = s)
			))
	
	implicit def convertScalaListToJavaList(list: List[String]) =
		java.util.Arrays.asList(list.toArray: _*)

	private def help
	{
		System.out.print("cowjac Java to C++ compiler\n")
		System.out.print("© 2012 David Given\n")
		System.out.print("\nSyntax: cowjac [<options>...]\n")
		System.out.print("\nOptions:\n")
		argumentParser.usage(System.out)
		
		System.exit(0)
	}
	
	private def error(message: String, args: AnyRef*)
	{
		System.err.println("Error: " + message.format(args))
		System.exit(1)
	}
	
	def main(argv: Array[String])
	{
		val inputfiles = argumentParser.wrappedProcess(argv, error(_, _))
		
		if ((inputjar == null) && (inputdir == null))
			error("You must specify an input jar or directory.")
		if ((inputjar != null) && (inputdir != null))
			error("You cannot specify both an input jar and a directory.")
		if (outputdir == null)
			error("You must specify an output directory.")
		if (!inputfiles.isEmpty)
			error("Extraneous files on command line (try --help)")
		if (mainclassname == null)
			error("You must specify the name of a main class")
		
		val sourcepath =
			if (inputjar != null) inputjar
			else if (inputdir != null) inputdir
			else null
			
		val filereader: FileSource =
			if (inputjar != null) new JarFileSource(inputjar)
			else if (inputdir != null) new DirectoryFileSource(inputdir)
			else null

		try
		{
			Options.v.set_soot_classpath(sourcepath)
			Options.v.set_process_dir(List(sourcepath))
			Options.v.set_prepend_classpath(false)
			//Options.v.set_whole_program(true)
			Options.v.set_main_class(mainclassname)
			Options.v.set_verbose(true)
			Options.v.set_include_all(true)
			Options.v.set_output_format(Options.output_format_jimple)
						
			Scene.v.loadNecessaryClasses
			PackManager.v.runPacks
			
			var mainclass = Scene.v.forceResolve(mainclassname, 0)
			//var cg = Scene.v.getCallGraph
			
			for (c <- Scene.v.getClasses)
			{
				Translator.translate(c, System.out, System.out)
			}
		}
		catch
		{
			case e: CompilationDeathException =>
				error("Compilation failed: %s", e.getMessage)
		}
	}
}
