package net.accela.prismatic.util;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * Various utility methods for working with arrays.
 * The methods are not guaranteed to be safe if you hand it the wrong objects,
 * so use at your own risk.
 */
public final class ArrayUtils {
    public static Byte[] bytesToByteObjects(byte[] bytes) {
        Byte[] byteObjects = new Byte[bytes.length];

        int i = 0;
        for (byte b : bytes) {
            byteObjects[i++] = b;
        }

        return byteObjects;
    }

    public static byte[] byteObjectsToBytes(Byte[] byteObjects) {
        byte[] bytes = new byte[byteObjects.length];

        int i = 0;
        for (Byte b : byteObjects) {
            bytes[i++] = b;
        }

        return bytes;
    }

    @SafeVarargs
    public static <T> T[] concatenateObjArrays(@NotNull T[]... arrays) {
        T[] all = null;
        for (T[] array : arrays) {
            if (all == null) {
                all = Arrays.copyOf(array, array.length);
                System.arraycopy(array, 0, all, 0, array.length);
            } else {
                all = Arrays.copyOf(all, all.length + array.length);
                System.arraycopy(array, 0, all, all.length, array.length);
            }
        }
        return all;
    }

    public static byte[] concatenateByte(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    public static <T> boolean contains(T[] array, T element) {
        for (T type : array) {
            if (type == element) return true;
        }
        return false;
    }
}
