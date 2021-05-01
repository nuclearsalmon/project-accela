package net.accela.prisma.util;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * Various utilities for working with arrays and byte arrays.
 * The generic methods are not guaranteed to be safe if you hand it the wrong objects,
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

    @Deprecated
    @SafeVarargs
    @SuppressWarnings("unchecked")
    public static <T> T[] concatenate1(T[]... arrays) {
        int totalLen = 0;
        for (T[] arr : arrays) {
            totalLen += arr.length;
        }

        T[] all = (T[]) Array.newInstance(
                arrays.getClass().getComponentType().getComponentType(), totalLen);

        int copied = 0;
        for (T[] arr : arrays) {
            System.arraycopy(arr, 0, all, copied, arr.length);
            copied += arr.length;
        }
        return all;
    }

    @Deprecated
    @SuppressWarnings("unchecked")
    public static <T> T[] concatenate2(T[] a, T[] b) {
        T[] result = (T[]) Array.newInstance(a.getClass().getComponentType(), a.length + b.length);
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    /**
     * Can concatenate both object and primitive types.
     */
    @Deprecated
    @SuppressWarnings("SuspiciousSystemArraycopy")
    public static <T> T concatenatePrimitive(T a, T b) {
        if (!a.getClass().isArray() || !b.getClass().isArray()) {
            throw new IllegalArgumentException();
        }

        Class<?> resCompType;
        Class<?> aCompType = a.getClass().getComponentType();
        Class<?> bCompType = b.getClass().getComponentType();

        if (aCompType.isAssignableFrom(bCompType)) {
            resCompType = aCompType;
        } else if (bCompType.isAssignableFrom(aCompType)) {
            resCompType = bCompType;
        } else {
            throw new IllegalArgumentException();
        }

        int aLen = Array.getLength(a);
        int bLen = Array.getLength(b);

        @SuppressWarnings("unchecked")
        T result = (T) Array.newInstance(resCompType, aLen + bLen);
        System.arraycopy(a, 0, result, 0, aLen);
        System.arraycopy(b, 0, result, aLen, bLen);

        return result;
    }

    @SafeVarargs
    public static <T> T[] concatenate3(@NotNull T[]... arrays) {
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
