package com.venkateshwaraphotos.shop.util;

import java.text.NumberFormat;
import java.util.Locale;

public final class Fmt {
    private Fmt() {}

    public static String money(double v) {
        return NumberFormat.getCurrencyInstance(new Locale("en", "IN")).format(v);
    }

    /** Plain 2-decimal number for pre-filling edit boxes. */
    public static String plain(double v) {
        return String.format(Locale.US, "%.2f", v);
    }

    public static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    public static Double parseDouble(String s) {
        try {
            return Double.valueOf(s.trim());
        } catch (Exception e) {
            return null;
        }
    }

    public static Integer parseInt(String s) {
        try {
            return Integer.valueOf(s.trim());
        } catch (Exception e) {
            return null;
        }
    }
}
