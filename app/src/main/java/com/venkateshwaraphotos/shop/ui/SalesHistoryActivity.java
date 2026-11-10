package com.venkateshwaraphotos.shop.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.venkateshwaraphotos.shop.R;
import com.venkateshwaraphotos.shop.data.entity.User;
import com.venkateshwaraphotos.shop.data.model.SaleRow;
import com.venkateshwaraphotos.shop.ui.adapter.RowAdapter;
import com.venkateshwaraphotos.shop.util.DateUtil;
import com.venkateshwaraphotos.shop.util.Fmt;
import com.venkateshwaraphotos.shop.viewmodel.HistoryViewModel;

import java.util.ArrayList;
import java.util.List;

/** Owner: all sales with date + employee filters. Employee: only their own sales (date filter only). */
public class SalesHistoryActivity extends BaseActivity {
    private HistoryViewModel vm;
    private MaterialButton btnFrom;
    private MaterialButton btnTo;
    private Spinner spEmployee;
    private long fromDay;
    private long toDay;
    private List<User> users = new ArrayList<>();
    private long employeeId;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!guard(false)) return;
        setContentView(R.layout.activity_list);
        final boolean owner = isOwner();
        toolbar = setupToolbar(owner ? "Sales History" : "My Sales");
        vm = new ViewModelProvider(this).get(HistoryViewModel.class);
        findViewById(R.id.fabAdd).setVisibility(View.GONE);

        long today = DateUtil.startOfDay(System.currentTimeMillis());
        toDay = today;
        fromDay = DateUtil.addDays(today, -6);

        LinearLayout filterBar = findViewById(R.id.filterBar);
        filterBar.setVisibility(View.VISIBLE);
        View filter = getLayoutInflater().inflate(R.layout.view_history_filter, filterBar, false);
        filterBar.addView(filter);
        btnFrom = filter.findViewById(R.id.btnFrom);
        btnTo = filter.findViewById(R.id.btnTo);
        spEmployee = filter.findViewById(R.id.spEmployee);
        btnFrom.setOnClickListener(v -> Ui.pickDate(this, fromDay, ts -> {
            fromDay = DateUtil.startOfDay(ts);
            apply();
        }));
        btnTo.setOnClickListener(v -> Ui.pickDate(this, toDay, ts -> {
            toDay = DateUtil.startOfDay(ts);
            apply();
        }));

        final View empty = findViewById(R.id.tvEmpty);
        RecyclerView rv = findViewById(R.id.recycler);
        rv.setLayoutManager(new LinearLayoutManager(this));
        final RowAdapter<SaleRow> adapter = new RowAdapter<SaleRow>(r -> RowBinders.sale(r, owner));
        adapter.onClick(r -> startActivity(new Intent(this, SaleDetailActivity.class).putExtra("saleId", r.sale.id)));
        rv.setAdapter(adapter);

        vm.list.observe(this, list -> {
            adapter.setItems(list);
            Ui.showEmpty(empty, list);
            double total = 0;
            if (list != null) {
                for (SaleRow r : list) total += r.sale.totalAmount;
            }
            toolbar.setSubtitle((list == null ? 0 : list.size()) + " sale(s)  |  " + Fmt.money(total));
        });

        if (owner) {
            vm.users().observe(this, list -> {
                users = list;
                List<String> names = new ArrayList<>();
                names.add("All employees");
                int keep = 0;
                for (int i = 0; i < list.size(); i++) {
                    names.add(list.get(i).fullName);
                    if (list.get(i).id == employeeId) keep = i + 1;
                }
                spEmployee.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, names));
                spEmployee.setSelection(keep);
            });
            spEmployee.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    long newId = position > 0 && position - 1 < users.size() ? users.get(position - 1).id : 0;
                    if (newId != employeeId) {
                        employeeId = newId;
                        apply();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        } else {
            spEmployee.setVisibility(View.GONE);
        }
        apply();
    }

    private void apply() {
        if (fromDay > toDay) {
            toast("The 'From' date is after the 'To' date.");
            return;
        }
        btnFrom.setText("From: " + DateUtil.date(fromDay));
        btnTo.setText("To: " + DateUtil.date(toDay));
        vm.setFilter(fromDay, DateUtil.addDays(toDay, 1), employeeId);
    }
}
