package com.cowlark.cava
import scala.collection.immutable.HashMap

object ArgumentParser
{
	type Descriptor = (String, String, Boolean, (String)=>Unit)
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
		if (a._1 != null)
			shortNames = shortNames + (a._1 -> a)
		if (a._2 != null)
			longNames = longNames + (a._2 -> a)
	}
	
	private def doarg(map: Map[String, ArgumentParser.Descriptor],
			arg: String, param: String): Integer =
	{
		if (!map.contains(arg))
			throw new UnrecognisedArgument(arg)
		
		val descriptor = map(arg)			
		if (descriptor._3 && (param == null))
			throw new MissingParameterToArgument(arg)
		if (!descriptor._3 && (param != null))
			throw new ExtraneousParameterToArgument(arg)
		
		descriptor._4(param)
		
		if (descriptor._3) 1 else 0
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

