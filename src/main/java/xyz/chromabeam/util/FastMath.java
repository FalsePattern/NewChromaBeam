package xyz.chromabeam.util;

public class FastMath {
    public static int floorMod(int a, int b) {
        return (a % b + b) % b;
    }
}
