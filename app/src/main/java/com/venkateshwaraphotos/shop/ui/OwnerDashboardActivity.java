package com.venkateshwaraphotos.shop.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.venkateshwaraphotos.shop.R;
import com.venkateshwaraphotos.shop.data.model.SaleRow;
import com.venkateshwaraphotos.shop.ui.adapter.RowAdapter;
import com.venkateshwaraphotos.shop.util.Fmt;
import com.venkateshwaraphotos.shop.viewmodel.DashboardViewModel;

public class OwnerDashboardActivity extends DashboardBase {
    private DashboardViewModel vm;
    private double monthGross;
    private double monthExpenses;
    private TextView tvMonthExpenses;
    private TextView tvMonthNet;
    private TextView tvMonthGross;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!guard(true)) return;
        setContentView(R.layout.activity_owner_dashboard);
        MaterialToolbar tb = setupToolbar("Venkateshwara Photos");
        setupMenu(tb, true);
        vm = new ViewModelProvider(this).get(DashboardViewModel.class);

        ((TextView) findViewById(R.id.tvGreeting)).setText("Welcome, " + app().session.getName());
        final TextView tvTodaySales = findViewById(R.id.tvTodaySales);
        final TextView tvTodayCount = findViewById(R.id.tvTodayCount);
        final TextView tvTodayProfit = findViewById(R.id.tvTodayProfit);
        tvMonthExpenses = findViewById(R.id.tvMonthExpenses);
        tvMonthNet = findViewById(R.id.tvMonthNet);
        tvMonthGross = findViewById(R.id.tvMonthGross);
        final TextView tvUnits = findViewById(R.id.tvUnits);
        final TextView tvInvValue = findViewById(R.id.tvInvValue);
        final TextView tvLow = findViewById(R.id.tvLow);
        final TextView tvLowHint = findViewById(R.id.tvLowHint);
        final TextView tvEmpty = findViewById(R.id.tvEmpty);

        RecyclerView rv = findViewById(R.id.recycler);
        rv.setLayoutManager(new LinearLayoutManager(this));
        final RowAdapter<SaleRow> adapter = new RowAdapter<SaleRow>(r -> RowBinders.sale(r, true));
        adapter.onClick(r -> openSale(r.sale.id));
        rv.setAdapter(adapter);

        vm.today.observe(this, s -> {
            if (s == null) return;
            tvTodaySales.setText(Fmt.money(s.revenue));
            tvTodayCount.setText(s.saleCount + " sale(s)");
            tvTodayProfit.setText(Fmt.money(s.gross()));
        });
        vm.month.observe(this, s -> {
            monthGross = s == null ? 0 : s.gross();
            renderMonth();
        });
        vm.monthExpenses.observe(this, e -> {
            monthExpenses = e == null ? 0 : e;
            renderMonth();
        });
        vm.units.observe(this, u -> tvUnits.setText((u == null ? 0 : u) + " units"));
        vm.inventoryValue.observe(this, v -> tvInvValue.setText("Value at cost " + Fmt.money(v == null ? 0 : v)));
        vm.lowCount.observe(this, n -> {
            int c = n == null ? 0 : n;
            tvLow.setText(String.valueOf(c));
            tvLow.setTextColor(getResources().getColor(c > 0 ? R.color.negative : R.color.primary));
            tvLowHint.setText(c > 0 ? "Tap to review" : "All stocked");
        });
        vm.recent.observe(this, list -> {
            adapter.setItems(list);
            Ui.showEmpty(tvEmpty, list);
        });

        findViewById(R.id.cardLow).setOnClickListener(v ->
                startActivity(new Intent(this, InventoryActivity.class).putExtra("lowOnly", true)));
        go(R.id.btnNewSale, NewSaleActivity.class);
        go(R.id.btnHistory, SalesHistoryActivity.class);
        go(R.id.btnInventory, InventoryActivity.class);
        go(R.id.btnPurchases, PurchaseActivity.class);
        go(R.id.btnCategories, CategoryActivity.class);
        go(R.id.btnProducts, ProductActivity.class);
        go(R.id.btnExpenses, ExpensesActivity.class);
        go(R.id.btnReports, ReportsActivity.class);
        go(R.id.btnEmployees, EmployeesActivity.class);
    }

    private void go(int buttonId, final Class<?> target) {
        findViewById(buttonId).setOnClickListener(v -> startActivity(new Intent(this, target)));
    }

    private void openSale(long id) {
        startActivity(new Intent(this, SaleDetailActivity.class).putExtra("saleId", id));
    }

    private void renderMonth() {
        tvMonthExpenses.setText(Fmt.money(monthExpenses));
        tvMonthGross.setText("Gross profit " + Fmt.money(monthGross));
        tvMonthNet.setText(Fmt.money(monthGross - monthExpenses));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (vm != null) vm.refresh();
    }
}
