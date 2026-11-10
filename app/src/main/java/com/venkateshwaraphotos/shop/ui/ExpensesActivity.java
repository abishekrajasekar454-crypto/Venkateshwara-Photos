package com.venkateshwaraphotos.shop.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.venkateshwaraphotos.shop.R;
import com.venkateshwaraphotos.shop.data.entity.Expense;
import com.venkateshwaraphotos.shop.ui.adapter.RowAdapter;
import com.venkateshwaraphotos.shop.util.DateUtil;
import com.venkateshwaraphotos.shop.util.Fmt;
import com.venkateshwaraphotos.shop.viewmodel.ExpenseViewModel;

import java.util.Arrays;
import java.util.List;

/** Owner only: monthly expense list with add / edit / long-press delete. */
public class ExpensesActivity extends BaseActivity {
    private static final List<String> PRESETS = Arrays.asList(
            "Rent", "Electricity", "Salaries", "Transport", "Maintenance", "Supplies", "Other");

    private ExpenseViewModel vm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!guard(true)) return;
        setContentView(R.layout.activity_list);
        setupToolbar("Expenses");
        vm = new ViewModelProvider(this).get(ExpenseViewModel.class);

        LinearLayout filterBar = findViewById(R.id.filterBar);
        filterBar.setVisibility(View.VISIBLE);
        View bar = getLayoutInflater().inflate(R.layout.view_month_bar, filterBar, false);
        filterBar.addView(bar);
        final TextView tvPeriod = bar.findViewById(R.id.tvPeriod);
        final TextView tvSummary = bar.findViewById(R.id.tvSummary);
        tvPeriod.setText(vm.monthLabel());
        bar.findViewById(R.id.btnPrev).setOnClickListener(v -> {
            vm.shiftMonth(-1);
            tvPeriod.setText(vm.monthLabel());
        });
        bar.findViewById(R.id.btnNext).setOnClickListener(v -> {
            vm.shiftMonth(1);
            tvPeriod.setText(vm.monthLabel());
        });

        final View empty = findViewById(R.id.tvEmpty);
        RecyclerView rv = findViewById(R.id.recycler);
        rv.setLayoutManager(new LinearLayoutManager(this));
        final RowAdapter<Expense> adapter = new RowAdapter<Expense>(e -> new RowAdapter.Row(
                e.category, e.description, null, Fmt.money(e.amount), DateUtil.date(e.date)));
        adapter.onClick(this::showForm);
        adapter.onLongClick(e -> Ui.confirm(this, "Delete expense",
                "Delete " + e.category + " " + Fmt.money(e.amount) + "?",
                () -> vm.delete(e, Ui.cb(this, (Void r) -> toast("Expense deleted.")))));
        rv.setAdapter(adapter);

        vm.expenses.observe(this, list -> {
            adapter.setItems(list);
            Ui.showEmpty(empty, list);
        });
        vm.total.observe(this, t -> tvSummary.setText("Total: " + Fmt.money(t == null ? 0 : t)
                + "   (tap to edit, long-press to delete)"));
        findViewById(R.id.fabAdd).setOnClickListener(v -> showForm(null));
    }

    private void showForm(final Expense e) {
        long initialDate = e == null ? DateUtil.startOfDay(System.currentTimeMillis()) : e.date;
        new FormDialog(this, e == null ? "New expense" : "Edit expense")
                .choice("preset", "Expense category", PRESETS, 0)
                .text("custom", "Or type your own category", e == null ? "" : e.category, FormDialog.TEXT)
                .text("desc", "Description", e == null ? "" : e.description, FormDialog.SENTENCE)
                .text("amount", "Amount (₹)", e == null ? "" : Fmt.plain(e.amount), FormDialog.DECIMAL)
                .date("date", "Date", initialDate)
                .show(f -> {
                    Double amount = Fmt.parseDouble(f.text("amount"));
                    if (amount == null) {
                        f.error("Enter a valid amount.");
                        return;
                    }
                    String custom = f.text("custom");
                    String category = custom.isEmpty() ? PRESETS.get(f.choice("preset")) : custom;
                    vm.save(e == null ? 0 : e.id, category, f.text("desc"), amount,
                            DateUtil.startOfDay(f.dateValue("date")), f.<Long>callback(null));
                });
    }
}
