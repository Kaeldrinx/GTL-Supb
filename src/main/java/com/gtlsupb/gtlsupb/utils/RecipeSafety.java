package com.gtlsupb.gtlsupb.utils;

import java.math.BigInteger;
import java.util.List;

public final class RecipeSafety {

    private RecipeSafety() {}

    public static <T> T at(List<T> list, int index) {
        if (list == null || index < 0 || index >= list.size()) return null;
        return list.get(index);
    }

    public static long safeDivide(long total, long divisor, long fallback) {
        if (divisor <= 0L) return fallback;
        return total / divisor;
    }

    public static BigInteger safeDivide(BigInteger total, long divisor, BigInteger fallback) {
        if (total == null || divisor <= 0L) return fallback;
        return total.divide(BigInteger.valueOf(divisor));
    }

    public static long saturatedMultiply(long a, long b) {
        try {
            return Math.multiplyExact(a, b);
        } catch (ArithmeticException overflow) {
            return (a >= 0L) == (b >= 0L) ? Long.MAX_VALUE : Long.MIN_VALUE;
        }
    }

    public static int clamp(int value, int min, int max) {
        if (max < min) return min;
        return Math.max(min, Math.min(max, value));
    }

    public static long maxFeasible(long requested, java.util.function.LongPredicate fits) {
        if (requested <= 0) return 0;
        if (fits.test(requested)) return requested;
        long lower = 0;
        long upper = requested - 1;
        while (lower < upper) {

            long mid = lower + (upper - lower) / 2 + (upper - lower) % 2;
            if (fits.test(mid)) {
                lower = mid;
            } else {
                upper = mid - 1;
            }
        }
        return lower;
    }
}
