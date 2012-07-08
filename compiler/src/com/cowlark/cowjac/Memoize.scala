package com.cowlark.cowjac

class Memoize[-T, +R](f: T => R) extends (T => R)
{
	private[this] var vals = Map.empty[T, R]
	
	def apply(x: T): R =
	{
		val v = vals.get(x)
		if (v != None)
			v.get
		else
		{
			val y = f(x)
			vals += (x -> y)
			y
		}
	}
}

object Memoize
{
	def apply[T, R](f: T => R) =
		new Memoize(f)
}
