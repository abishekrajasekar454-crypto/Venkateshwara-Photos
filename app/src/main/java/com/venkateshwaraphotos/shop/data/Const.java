package com.venkateshwaraphotos.shop.data;

/** App-wide string constants (kept as strings so they are easy to sync to Firestore later). */
public final class Const {
    private Const() {}

    public static final String OWNER = "OWNER";
    public static final String EMPLOYEE = "EMPLOYEE";

    public static final String TRACK_VARIANT = "VARIANT";
    public static final String TRACK_DESIGN = "DESIGN";

    public static final String PAY_CASH = "CASH";
    public static final String PAY_UPI = "UPI";
    public static final String PAY_OTHER = "OTHER";

    public static final String MOVE_PURCHASE = "PURCHASE";
    public static final String MOVE_SALE = "SALE";
    public static final String MOVE_ADJUST = "ADJUSTMENT";
    public static final String MOVE_OPENING = "OPENING";

    public static final int DEFAULT_LOW_STOCK = 5;
}
