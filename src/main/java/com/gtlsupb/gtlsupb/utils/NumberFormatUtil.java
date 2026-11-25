package com.gtlsupb.gtlsupb.utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Locale;

public final class NumberFormatUtil {

    private static final BigInteger SCIENTIFIC_THRESHOLD = BigInteger.valueOf(1_000_000L);

    private NumberFormatUtil() {}

    public static String scientific(BigInteger value) {
        if (value == null) return "0";
        if (value.abs().compareTo(SCIENTIFIC_THRESHOLD) < 0) {
            return String.format(Locale.ROOT, "%,d", value);
        }
        return String.format(Locale.ROOT, "%.3E", new BigDecimal(value));
    }

    public static String scientific(long value) {
        return scientific(BigInteger.valueOf(value));
    }
}
