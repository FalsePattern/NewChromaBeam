package moe.falsepattern.util;

/**
 * FalsePattern Math.
 */
public final class FPMath {
    public static int nextPowerOfTwo(int value) {
        int i = 1;
        while (i < value) i <<= 1;
        return i;
    }

    public static int modulo(int value, int mod) {
        return ((value % mod) + mod) % mod;
    }
}
