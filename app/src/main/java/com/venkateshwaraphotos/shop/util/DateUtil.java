package com.venkateshwaraphotos.shop.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public final class DateUtil {
    private DateUtil() {}

    public static long startOfDay(long ts) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(ts);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    public static long addDays(long ts, int days) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(ts);
        c.add(Calendar.DAY_OF_MONTH, days);
        return c.getTimeInMillis();
    }

    /** Monday 00:00 of the week containing ts. */
    public static long startOfWeek(long ts) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(startOfDay(ts));
        int diff = (c.get(Calendar.DAY_OF_WEEK) + 5) % 7;
        c.add(Calendar.DAY_OF_MONTH, -diff);
        return c.getTimeInMillis();
    }

    public static long startOfMonth(long ts) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(startOfDay(ts));
        c.set(Calendar.DAY_OF_MONTH, 1);
        return c.getTimeInMillis();
    }

    public static long addMonths(long ts, int months) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(ts);
        c.add(Calendar.MONTH, months);
        return c.getTimeInMillis();
    }

    public static String date(long ts) {
        return new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date(ts));
    }

    public static String dateTime(long ts) {
        return new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(new Date(ts));
    }

    public static String month(long ts) {
        return new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(new Date(ts));
    }
}
