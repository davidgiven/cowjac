package com.cowlark.cowjac
import java.io.Writer
import java.util.Formatter
import java.lang.CharSequence

class Printer
{
	private var stream = Vector.empty[String]
	
	def print(s: String) =
		stream :+= s
		
	def print(s1: String, s2: String) =
	{
		stream :+= s1
		stream :+= s2
	}
		
	def print(s1: String, s2: String, s3: String) =
	{
		stream :+= s1
		stream :+= s2
		stream :+= s3
	}
		
	def print(s1: String, s2: String, s3: String, s4: String) =
	{
		stream :+= s1
		stream :+= s2
		stream :+= s3
		stream :+= s4
	}
		
	def print(s1: String, s2: String, s3: String, s4: String, s5: String) =
	{
		stream :+= s1
		stream :+= s2
		stream :+= s3
		stream :+= s4
		stream :+= s5
	}
		
	def print(s1: String, s2: String, s3: String, s4: String, s5: String,
			s6: String)
	{
		stream :+= s1
		stream :+= s2
		stream :+= s3
		stream :+= s4
		stream :+= s5
		stream :+= s6
	}
		
	def print(s1: String, s2: String, s3: String, s4: String, s5: String,
			s6: String, s7: String)
	{
		stream :+= s1
		stream :+= s2
		stream :+= s3
		stream :+= s4
		stream :+= s5
		stream :+= s6
		stream :+= s7
	}
		
	def emit(ps: Writer)
	{
		for (s <- stream)
			ps.write(s)
	}
}

case class PrintSet(h: Printer = new Printer,
		c: Printer = new Printer,
		ch: Printer = new Printer)