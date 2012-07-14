package java.nio.charset;

import org.apache.harmony.niochar.charset.UTF_8;

public abstract class Charset implements Comparable<Charset>
{
	private static Charset _defaultCharset = new UTF_8("UTF-8", null);
	
	public static Charset defaultCharset()
	{
		return _defaultCharset;
	}
	
	public static boolean isSupported(String encoding)
	{
		return encoding.equals("UTF-8");
	}

	public static Charset forName(String encoding) throws UnsupportedCharsetException
	{
		if (encoding.equals(_defaultCharset.name()))
			return _defaultCharset;
		throw new UnsupportedCharsetException(encoding);
	}
	
	private static String _canonicalName;
	
	public Charset(String canonicalName, String[] aliases)
	{
		_canonicalName = canonicalName;
	}
	
	public String name()
	{
		return _canonicalName;
	}
	
    public int compareTo(Charset charset) {
        return this._canonicalName.compareToIgnoreCase(charset._canonicalName);
    }

	public abstract boolean contains(Charset cs);
    public abstract CharsetDecoder newDecoder();
    public abstract CharsetEncoder newEncoder();

}
