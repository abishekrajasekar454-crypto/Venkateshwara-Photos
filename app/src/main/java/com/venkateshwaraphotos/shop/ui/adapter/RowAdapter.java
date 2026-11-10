package com.venkateshwaraphotos.shop.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.venkateshwaraphotos.shop.R;

import java.util.ArrayList;
import java.util.List;

/**
 * One generic RecyclerView adapter used by every list in the app. Each screen supplies a Binder that
 * turns its item type into a Row of up to five strings (see item_row.xml).
 */
public class RowAdapter<T> extends RecyclerView.Adapter<RowAdapter.VH> {

    public static class Row {
        public final String title;
        public final String subtitle;
        public final String meta;
        public final String right;
        public final String right2;
        public boolean warn;

        public Row(String title, String subtitle, String meta, String right, String right2) {
            this.title = title;
            this.subtitle = subtitle;
            this.meta = meta;
            this.right = right;
            this.right2 = right2;
        }

        public Row warn(boolean w) {
            this.warn = w;
            return this;
        }
    }

    public interface Binder<T> {
        Row bind(T item);
    }

    public interface OnItem<T> {
        void onItem(T item);
    }

    public static class VH extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView subtitle;
        final TextView meta;
        final TextView right;
        final TextView right2;
        final int defaultRightColor;

        VH(View v) {
            super(v);
            title = v.findViewById(R.id.tvTitle);
            subtitle = v.findViewById(R.id.tvSubtitle);
            meta = v.findViewById(R.id.tvMeta);
            right = v.findViewById(R.id.tvRight);
            right2 = v.findViewById(R.id.tvRight2);
            defaultRightColor = right.getCurrentTextColor();
        }
    }

    private final List<T> items = new ArrayList<>();
    private final Binder<T> binder;
    private OnItem<T> onClick;
    private OnItem<T> onLongClick;

    public RowAdapter(Binder<T> binder) {
        this.binder = binder;
    }

    public RowAdapter<T> onClick(OnItem<T> l) {
        onClick = l;
        return this;
    }

    public RowAdapter<T> onLongClick(OnItem<T> l) {
        onLongClick = l;
        return this;
    }

    public void setItems(List<T> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(VH h, int position) {
        final T item = items.get(position);
        Row r = binder.bind(item);
        set(h.title, r.title);
        set(h.subtitle, r.subtitle);
        set(h.meta, r.meta);
        set(h.right, r.right);
        set(h.right2, r.right2);
        h.right.setTextColor(r.warn
                ? h.itemView.getResources().getColor(R.color.negative)
                : h.defaultRightColor);
        h.itemView.setOnClickListener(onClick == null ? null : v -> onClick.onItem(item));
        h.itemView.setOnLongClickListener(onLongClick == null ? null : v -> {
            onLongClick.onItem(item);
            return true;
        });
    }

    private static void set(TextView tv, String s) {
        if (s == null || s.isEmpty()) {
            tv.setVisibility(View.GONE);
        } else {
            tv.setVisibility(View.VISIBLE);
            tv.setText(s);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
