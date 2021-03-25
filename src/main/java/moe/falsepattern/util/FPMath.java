package moe.falsepattern.util;

public class FPMath {
    public static int nextPowerOfTwo(int value) {
        int i = 1;
        while (i < value) i <<= 1;
        return i;
    }
}
