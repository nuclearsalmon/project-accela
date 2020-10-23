package net.accela.telnet.util;

import java.lang.reflect.Array;

public final class ArrayUtil {
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
    public static <T> T[] mergeArrays(T[]... arrays) {
        int totalLen = 0;
        for (T[] arr : arrays) {
            totalLen += arr.length;
        }

        @SuppressWarnings("unchecked")
        T[] all = (T[]) Array.newInstance(
                arrays.getClass().getComponentType().getComponentType(), totalLen);

        int copied = 0;
        for (T[] arr : arrays) {
            System.arraycopy(arr, 0, all, copied, arr.length);
            copied += arr.length;
        }
        return all;
    }
}
