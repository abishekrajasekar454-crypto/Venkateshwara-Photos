package com.venkateshwaraphotos.shop.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.venkateshwaraphotos.shop.R;
import com.venkateshwaraphotos.shop.data.model.PaymentTotal;
import com.venkateshwaraphotos.shop.data.model.TopItem;
import com.venkateshwaraphotos.shop.util.Fmt;
import com.venkateshwaraphotos.shop.viewmodel.ReportViewModel;

/**
 * Owner only: daily / weekly / monthly report.
 * Gross profit = revenue - COGS. Net profit = gross profit - expenses (purchases are not expenses).
 */
public class ReportsActivity extends BaseActivity {
    private double gross;
    private double expenses;
    private TextView tvNet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!guard(true)) return;
        setContentView(R.layout.activity_reports);
        setupToolbar("Reports");
        final ReportViewModel vm = new ViewModelProvider(this).get(ReportViewModel.class);

        MaterialButton tab1 = findViewById(R.id.tab1);
        MaterialButton tab2 = findViewById(R.id.tab2);
        MaterialButton tab3 = findViewById(R.id.tab3);
        tab1.setText("Daily");
        tab2.setText("Weekly");
        tab3.setText("Monthly");
        tab3.setVisibility(View.VISIBLE);
        MaterialButtonToggleGroup group = findViewById(R.id.tabGroup);
        group.check(vm.getMode() == ReportViewModel.WEEKLY ? R.id.tab2
                : vm.getMode() == ReportViewModel.MONTHLY ? R.id.tab3 : R.id.tab1);
        group.addOnButtonCheckedListener((g, checkedId, isChecked) -> {
            if (!isChecked) return;
            vm.setMode(checkedId == R.id.tab2 ? ReportViewModel.WEEKLY
                    : checkedId == R.id.tab3 ? ReportViewModel.MONTHLY : ReportViewModel.DAILY);
        });

        final TextView tvPeriod = findViewById(R.id.tvPeriod);
        findViewById(R.id.tvSummary).setVisibility(View.GONE);
        findViewById(R.id.btnPrev).setOnClickListener(v -> vm.shift(-1));
        findViewById(R.id.btnNext).setOnClickListener(v -> vm.shift(1));
        tvPeriod.setOnClickListener(v -> vm.goToToday()); // tap the period label to jump back to today

        final TextView tvRevenue = findViewById(R.id.tvRevenue);
        final TextView tvSaleCount = findViewById(R.id.tvSaleCount);
        final TextView tvCogs = findViewById(R.id.tvCogs);
        final TextView tvGross = findViewById(R.id.tvGross);
        final TextView tvExpenses = findViewById(R.id.tvExpenses);
        tvNet = findViewById(R.id.tvNet);
        final TextView tvPayments = findViewById(R.id.tvPayments);
        final TextView tvTop = findViewById(R.id.tvTop);

        vm.label.observe(this, text -> tvPeriod.setText(text));
        vm.summary.observe(this, s -> {
            if (s == null) return;
            tvRevenue.setText(Fmt.money(s.revenue));
            tvSaleCount.setText(s.saleCount + " sale(s)");
            tvCogs.setText(Fmt.money(s.cogs));
            gross = s.gross();
            tvGross.setText(Fmt.money(gross));
            renderNet();
        });
        vm.expenses.observe(this, e -> {
            expenses = e == null ? 0 : e;
            tvExpenses.setText(Fmt.money(expenses));
            renderNet();
        });
        vm.payments.observe(this, list -> {
            StringBuilder sb = new StringBuilder();
            if (list != null) {
                for (PaymentTotal p : list) {
                    sb.append(p.mode).append(":  ").append(Fmt.money(p.amount))
                            .append("  (").append(p.cnt).append(" sale(s))\n");
                }
            }
            tvPayments.setText(sb.length() == 0 ? "No sales in this period." : sb.toString().trim());
        });
        vm.top.observe(this, list -> {
            StringBuilder sb = new StringBuilder();
            if (list != null) {
                int n = 1;
                for (TopItem t : list) {
                    sb.append(n++).append(". ").append(t.name).append("  -  ").append(t.qty)
                            .append(" sold, ").append(Fmt.money(t.revenue)).append("\n");
                }
            }
            tvTop.setText(sb.length() == 0 ? "No sales in this period." : sb.toString().trim());
        });
    }

    private void renderNet() {
        double net = gross - expenses;
        tvNet.setText(Fmt.money(net));
        tvNet.setTextColor(getResources().getColor(net < 0 ? R.color.negative : R.color.positive));
    }
}
