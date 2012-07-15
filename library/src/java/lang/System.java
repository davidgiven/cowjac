package java.lang;

import com.cowlark.cowjac.harmony.Native;

public class System
{
	public static String getProperty(String key)
	{
		return "";
	}
	
	public static String getProperty(String key, String defaultValue)
	{
		return defaultValue;
	}

	   /**
     * Copies the number of {@code length} elements of the Array {@code src}
     * starting at the offset {@code srcPos} into the Array {@code dest} at
     * the position {@code destPos}.
     *
     * @param src
     *            the source array to copy the content.
     * @param srcPos
     *            the starting index of the content in {@code src}.
     * @param dest
     *            the destination array to copy the data into.
     * @param destPos
     *            the starting index for the copied content in {@code dest}.
     * @param length
     *            the number of elements of the {@code array1} content they have
     *            to be copied.
     */
    public static void arraycopy(Object src, int srcPos, Object dest, int destPos,
            int length) {
        // sending getClass() to both arguments will check for null
        Class<?> type1 = src.getClass();
        Class<?> type2 = dest.getClass();
        if (!type1.isArray() || !type2.isArray()) {
            throw new ArrayStoreException();
        }
        Class<?> componentType1 = type1.getComponentType();
        Class<?> componentType2 = type2.getComponentType();
        if (!componentType1.isPrimitive()) {
            if (componentType2.isPrimitive()) {
                throw new ArrayStoreException();
            }
            arraycopy((Object[]) src, srcPos, (Object[]) dest, destPos, length);
        } else {
            if (componentType2 != componentType1) {
                throw new ArrayStoreException();
            }
            if (componentType1 == Integer.TYPE) {
                arraycopy((int[]) src, srcPos, (int[]) dest, destPos, length);
            } else if (componentType1 == Byte.TYPE) {
                arraycopy((byte[]) src, srcPos, (byte[]) dest, destPos, length);
            } else if (componentType1 == Long.TYPE) {
                arraycopy((long[]) src, srcPos, (long[]) dest, destPos, length);
            } else if (componentType1 == Short.TYPE) {
                arraycopy((short[]) src, srcPos, (short[]) dest, destPos, length);
            } else if (componentType1 == Character.TYPE) {
                arraycopy((char[]) src, srcPos, (char[]) dest, destPos, length);
            } else if (componentType1 == Boolean.TYPE) {
                arraycopy((boolean[]) src, srcPos, (boolean[]) dest, destPos, length);
            } else if (componentType1 == Double.TYPE) {
                arraycopy((double[]) src, srcPos, (double[]) dest, destPos, length);
            } else if (componentType1 == Float.TYPE) {
                arraycopy((float[]) src, srcPos, (float[]) dest, destPos, length);
            }
        }
    }

	@Native("currentTimeMillis")
	public static native long currentTimeMillis();
	
}
