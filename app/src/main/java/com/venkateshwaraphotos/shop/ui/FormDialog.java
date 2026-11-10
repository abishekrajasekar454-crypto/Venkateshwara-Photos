package com.venkateshwaraphotos.shop.ui;

import android.content.Context;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.venkateshwaraphotos.shop.R;
import com.venkateshwaraphotos.shop.util.Callback;
import com.venkateshwaraphotos.shop.util.DateUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds a simple add/edit form dialog in code (text boxes, drop-downs, check boxes, date picker).
 * Used for all the small create/edit forms so the app has one consistent, tested form implementation.
 */
public class FormDialog {
    public static final int TEXT = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS;
    public static final int SENTENCE = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES;
    public static final int PLAIN = InputType.TYPE_CLASS_TEXT;
    public static final int DECIMAL = InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL;
    public static final int INTEGER = InputType.TYPE_CLASS_NUMBER;
    public static final int SIGNED_INT = InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED;
    public static final int PASSWORD = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD;

    public interface OnSubmit {
        void onSubmit(FormDialog form);
    }

    private final Context ctx;
    private final String title;
    private final LinearLayout container;
    private final TextView errorView;
    private final Map<String, TextInputEditText> texts = new HashMap<>();
    private final Map<String, Spinner> spinners = new HashMap<>();
    private final Map<String, CheckBox> checks = new HashMap<>();
    private final Map<String, long[]> dates = new HashMap<>();
    private String positiveLabel = "Save";
    private AlertDialog dialog;

    public FormDialog(Context ctx, String title) {
        this.ctx = ctx;
        this.title = title;
        container = new LinearLayout(ctx);
        container.setOrientation(LinearLayout.VERTICAL);
        int pad = Ui.dp(ctx, 20);
        container.setPadding(pad, Ui.dp(ctx, 8), pad, 0);
        errorView = new TextView(ctx);
        errorView.setTextColor(ctx.getResources().getColor(R.color.negative));
        errorView.setVisibility(View.GONE);
        container.addView(errorView);
    }

    private LinearLayout.LayoutParams lp(int topDp) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        p.topMargin = Ui.dp(ctx, topDp);
        return p;
    }

    public FormDialog positiveLabel(String label) {
        positiveLabel = label;
        return this;
    }

    public FormDialog info(String text) {
        TextView tv = new TextView(ctx);
        tv.setText(text);
        container.addView(tv, lp(8));
        return this;
    }

    public FormDialog text(String key, String label, String initial, int inputType) {
        TextInputLayout til = new TextInputLayout(ctx);
        til.setHint(label);
        TextInputEditText et = new TextInputEditText(til.getContext());
        et.setInputType(inputType);
        et.setSingleLine(true);
        if (initial != null) et.setText(initial);
        if (inputType == PASSWORD) {
            til.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
        }
        til.addView(et, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        container.addView(til, lp(10));
        texts.put(key, et);
        return this;
    }

    public FormDialog choice(String key, String label, List<String> options, int selected) {
        TextView tv = new TextView(ctx);
        tv.setText(label);
        tv.setTextSize(12);
        container.addView(tv, lp(12));
        Spinner sp = new Spinner(ctx);
        sp.setAdapter(new ArrayAdapter<>(ctx, android.R.layout.simple_spinner_dropdown_item, options));
        if (selected >= 0 && selected < options.size()) sp.setSelection(selected);
        container.addView(sp, lp(0));
        spinners.put(key, sp);
        return this;
    }

    public FormDialog check(String key, String label, boolean initial) {
        CheckBox cb = new CheckBox(ctx);
        cb.setText(label);
        cb.setChecked(initial);
        container.addView(cb, lp(8));
        checks.put(key, cb);
        return this;
    }

    public FormDialog date(String key, final String label, long initial) {
        final long[] holder = new long[]{initial};
        final MaterialButton btn = new MaterialButton(ctx, null,
                com.google.android.material.R.attr.materialButtonOutlinedStyle);
        btn.setText(label + ": " + DateUtil.date(initial));
        btn.setOnClickListener(v -> Ui.pickDate(ctx, holder[0], ts -> {
            holder[0] = ts;
            btn.setText(label + ": " + DateUtil.date(ts));
        }));
        container.addView(btn, lp(10));
        dates.put(key, holder);
        return this;
    }

    // ----- values -----
    public String text(String key) {
        TextInputEditText et = texts.get(key);
        return et == null || et.getText() == null ? "" : et.getText().toString().trim();
    }

    public int choice(String key) {
        Spinner sp = spinners.get(key);
        return sp == null ? -1 : sp.getSelectedItemPosition();
    }

    public boolean checked(String key) {
        CheckBox cb = checks.get(key);
        return cb != null && cb.isChecked();
    }

    public long dateValue(String key) {
        long[] h = dates.get(key);
        return h == null ? 0 : h[0];
    }

    // ----- lifecycle -----
    public void error(String message) {
        errorView.setText(message);
        errorView.setVisibility(View.VISIBLE);
    }

    public void close() {
        if (dialog != null) dialog.dismiss();
    }

    /** Callback that closes the dialog on success and shows the message inside the dialog on error. */
    public <T> Callback<T> callback(final Runnable afterSuccess) {
        return new Callback<T>() {
            @Override
            public void onSuccess(T result) {
                close();
                if (afterSuccess != null) afterSuccess.run();
            }

            @Override
            public void onError(String message) {
                error(message);
            }
        };
    }

    public void show(final OnSubmit onSubmit) {
        ScrollView sv = new ScrollView(ctx);
        sv.addView(container);
        dialog = new MaterialAlertDialogBuilder(ctx)
                .setTitle(title)
                .setView(sv)
                .setPositiveButton(positiveLabel, null)
                .setNegativeButton("Cancel", null)
                .create();
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            errorView.setVisibility(View.GONE);
            onSubmit.onSubmit(FormDialog.this);
        }));
        dialog.show();
    }
}
