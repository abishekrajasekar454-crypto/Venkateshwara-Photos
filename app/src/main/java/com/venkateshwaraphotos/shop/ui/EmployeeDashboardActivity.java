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

public class EmployeeDashboardActivity extends DashboardBase {
    private DashboardViewModel vm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!guard(false)) return;
        setContentView(R.layout.activity_employee_dashboard);
        MaterialToolbar tb = setupToolbar("Venkateshwara Photos");
        setupMenu(tb, false);
        vm = new ViewModelProvider(this).get(DashboardViewModel.class);

        ((TextView) findViewById(R.id.tvGreeting)).setText("Hello, " + app().session.getName());
        final TextView tvSales = findViewById(R.id.tvTodaySales);
        final TextView tvCount = findViewById(R.id.tvTodayCount);
        final TextView tvLow = findViewById(R.id.tvLow);
        final TextView tvEmpty = findViewById(R.id.tvEmpty);

        RecyclerView rv = findViewById(R.id.recycler);
        rv.setLayoutManager(new LinearLayoutManager(this));
        // Employees never see profit figures.
        final RowAdapter<SaleRow> adapter = new RowAdapter<SaleRow>(r -> RowBinders.sale(r, false));
        adapter.onClick(r -> startActivity(new Intent(this, SaleDetailActivity.class).putExtra("saleId", r.sale.id)));
        rv.setAdapter(adapter);

        vm.today.observe(this, s -> {
            if (s == null) return;
            tvSales.setText(Fmt.money(s.revenue));
            tvCount.setText(s.saleCount + " sale(s)");
        });
        vm.lowCount.observe(this, n -> tvLow.setText(String.valueOf(n == null ? 0 : n)));
        vm.recent.observe(this, list -> {
            adapter.setItems(list);
            Ui.showEmpty(tvEmpty, list);
        });

        findViewById(R.id.cardLow).setOnClickListener(v ->
                startActivity(new Intent(this, InventoryActivity.class).putExtra("lowOnly", true)));
        go(R.id.btnNewSale, NewSaleActivity.class);
        go(R.id.btnInventory, InventoryActivity.class);
        go(R.id.btnHistory, SalesHistoryActivity.class);
    }

    private void go(int buttonId, final Class<?> target) {
        findViewById(buttonId).setOnClickListener(v -> startActivity(new Intent(this, target)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (vm != null) vm.refresh();
    }
}
