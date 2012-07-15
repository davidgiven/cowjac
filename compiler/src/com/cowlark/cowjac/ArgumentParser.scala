/* cowjac © 2012 David Given
 * This file is licensed under the Simplified BSD license. Please see
 * COPYING.cowjac for the full text.
 */

package com.cowlark.cowjac
import scala.collection.immutable.HashMap
import java.io.PrintStream

object ArgumentParser
{
	case class Descriptor(shortName: String, longName: String,
			hasParameter: Boolean, help: String, cb: (String)=>Unit)
}

class ArgumentParserException extends Exception
{
}

class MissingParameterToArgument(arg: String) extends ArgumentParserException
{
	override def getMessage = "Parameter '%s' requires an argument.".format(arg)
}

class ExtraneousParameterToArgument(arg: String) extends ArgumentParserException
{
	override def getMessage = "Parameter '%s' must not take an argument.".format(arg)
}

class UnrecognisedArgument(arg: String) extends ArgumentParserException
{
	override def getMessage = "Parameter '%s' is not recognised; try --help.".format(arg)
}

class ArgumentParser(arguments: Array[ArgumentParser.Descriptor])
{
	private var shortNames = new HashMap[String, ArgumentParser.Descriptor]
	private var longNames = new HashMap[String, ArgumentParser.Descriptor]
	
	for (a <- arguments)
	{
		if (a.shortName != null)
			shortNames += (a.shortName -> a)
		if (a.longName != null)
			longNames += (a.longName -> a)
	}
	
	private def doarg(map: Map[String, ArgumentParser.Descriptor],
			arg: String, param: String): Integer =
	{
		if (!map.contains(arg))
			throw new UnrecognisedArgument(arg)
		
		val descriptor = map(arg)			
		if (descriptor.hasParameter && (param == null))
			throw new MissingParameterToArgument(arg)
		if (!descriptor.hasParameter && (param != null))
			throw new ExtraneousParameterToArgument(arg)
		
		descriptor.cb(param)
		
		if (descriptor.hasParameter) 1 else 0
	}
	
	def usage(ps: PrintStream)
	{
		for (a <- arguments)
		{
			var sb = new StringBuilder
			
			sb ++= (if (a.shortName.isEmpty) "  " else "-"+a.shortName)
			sb ++= (if (a.shortName.isEmpty) " " else if (a.hasParameter) "X" else " ")
			sb ++= "   --"
			sb ++= a.longName
			if (a.hasParameter) sb ++= "=X"

			ps.println(String.format("  %-20s  %s", sb, a.help))
		}
	}
	
	def process(argv: IndexedSeq[String]): Seq[String] =
	{
		var outputFiles = Vector.empty[String]
		
		var i = 1
		while (i < argv.length)
		{
			val arg = argv(i)
			i = i + 1
			
			if ((arg.size > 0) && (arg.charAt(0) == '-'))
			{
				/* An argument, of some kind */
				
				if ((arg.size > 1) && (arg.charAt(1) == '-'))
				{
					/* A long argument */
					
					val equals = arg.findIndexOf((c) => (c=='='))
					val name = if (equals == -1) arg.substring(2) else
						arg.substring(2, equals)
					
					if (equals == -1)
					{
						/* Out of line parameter. */
						
						val param = if (i >= argv.length) null else	argv(i)
						i = i + doarg(longNames, name, param)
					}
					else
					{
						/* In line parameter. */

						val param = if (equals == -1) null else
							arg.substring(equals+1)
						doarg(longNames, name, param)
					}
				}
				else
				{
					/* A short argument */
					
					val name = arg.substring(1, 2)
					val param = if (i >= argv.length) null else
						argv(i)
						
					i = i + doarg(shortNames, name, param)
				}
			}
			else
			{
				/* Not an argument. */
				
				outputFiles = outputFiles :+ arg
			}
		}
		
		outputFiles
	}
	
	def wrappedProcess(argv: IndexedSeq[String],
			error: (String, AnyRef*)=>Unit): Seq[String] =
	{
		try
		{
			return process(argv)
		}
		catch
		{
			case e: ArgumentParserException =>
				error(e.getMessage)
				null
		}
	}
}

