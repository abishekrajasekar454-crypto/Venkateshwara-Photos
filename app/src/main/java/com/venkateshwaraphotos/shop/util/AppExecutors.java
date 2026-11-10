package com.venkateshwaraphotos.shop.util;

import android.database.sqlite.SQLiteConstraintException;
import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Runs database work on a single background thread and delivers results on the main thread. */
public class AppExecutors {
    /** Single thread => writes are serialised, which keeps stock arithmetic consistent. */
    public final ExecutorService diskIO = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());

    public <T> void run(final Callable<T> work, final Callback<T> cb) {
        diskIO.execute(() -> {
            try {
                final T result = work.call();
                if (cb != null) {
                    main.post(() -> cb.onSuccess(result));
                }
            } catch (ShopException e) {
                fail(cb, e.getMessage());
            } catch (SQLiteConstraintException e) {
                fail(cb, "That record already exists (duplicate name) or is still in use.");
            } catch (Exception e) {
                fail(cb, "Something went wrong: " + e.getMessage());
            }
        });
    }

    private <T> void fail(final Callback<T> cb, final String message) {
        if (cb != null) {
            main.post(() -> cb.onError(message));
        }
    }
}
