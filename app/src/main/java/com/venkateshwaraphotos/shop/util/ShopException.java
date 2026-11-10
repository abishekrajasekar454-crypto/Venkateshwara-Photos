package com.venkateshwaraphotos.shop.util;

/** A business-rule failure whose message is safe to show to the user. Rolls back Room transactions. */
public class ShopException extends RuntimeException {
    public ShopException(String message) {
        super(message);
    }
}
