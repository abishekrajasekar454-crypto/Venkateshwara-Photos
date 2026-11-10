package com.venkateshwaraphotos.shop.ui;

import android.app.DatePickerDialog;
import android.content.Context;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.venkateshwaraphotos.shop.util.Callback;

import java.util.Calendar;
import java.util.List;
import java.util.function.Consumer;

/** Small UI helpers shared by all screens. */
public final class Ui {
    private Ui() {}

    public interface DateListener {
        void onDate(long timestamp);
    }

    /** Callback that runs onOk on success and shows a toast on error. */
    public static <T> Callback<T> cb(final Context ctx, final Consumer<T> onOk) {
        return new Callback<T>() {
            @Override
            public void onSuccess(T result) {
                onOk.accept(result);
            }

            @Override
            public void onError(String message) {
                toast(ctx, message);
            }
        };
    }

    public static void toast(Context ctx, String message) {
        Toast.makeText(ctx, message, Toast.LENGTH_LONG).show();
    }

    public static int dp(Context ctx, int dp) {
        return Math.round(dp * ctx.getResources().getDisplayMetrics().density);
    }

    public static void confirm(Context ctx, String title, String message, final Runnable onYes) {
        new MaterialAlertDialogBuilder(ctx)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Yes", (d, w) -> onYes.run())
                .setNegativeButton("Cancel", null)
                .show();
    }

    public static void pickDate(Context ctx, long initial, final DateListener listener) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(initial);
        new DatePickerDialog(ctx, (view, year, month, day) -> {
            Calendar r = Calendar.getInstance();
            r.set(year, month, day, 0, 0, 0);
            r.set(Calendar.MILLISECOND, 0);
            listener.onDate(r.getTimeInMillis());
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    public static void showEmpty(View emptyView, List<?> list) {
        emptyView.setVisibility(list == null || list.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
