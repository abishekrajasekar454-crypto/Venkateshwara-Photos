package com.venkateshwaraphotos.shop.util;

/** Result callback delivered on the main thread. */
public interface Callback<T> {
    void onSuccess(T result);

    void onError(String message);
}
