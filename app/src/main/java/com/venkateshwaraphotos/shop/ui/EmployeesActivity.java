package com.venkateshwaraphotos.shop.ui;

import android.os.Bundle;
import android.view.View;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.venkateshwaraphotos.shop.R;
import com.venkateshwaraphotos.shop.data.entity.User;
import com.venkateshwaraphotos.shop.ui.adapter.RowAdapter;
import com.venkateshwaraphotos.shop.viewmodel.EmployeeViewModel;

/** Owner only: create employees, rename / disable them and reset their passwords. */
public class EmployeesActivity extends BaseActivity {
    private EmployeeViewModel vm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!guard(true)) return;
        setContentView(R.layout.activity_list);
        setupToolbar("Employees");
        vm = new ViewModelProvider(this).get(EmployeeViewModel.class);

        final View empty = findViewById(R.id.tvEmpty);
        RecyclerView rv = findViewById(R.id.recycler);
        rv.setLayoutManager(new LinearLayoutManager(this));
        final RowAdapter<User> adapter = new RowAdapter<User>(u -> new RowAdapter.Row(
                u.fullName, "Username: " + u.username, null, u.active ? "Active" : "Disabled", null));
        adapter.onClick(this::showActions);
        rv.setAdapter(adapter);

        vm.employees().observe(this, list -> {
            adapter.setItems(list);
            Ui.showEmpty(empty, list);
        });
        findViewById(R.id.fabAdd).setOnClickListener(v -> showCreate());
    }

    private void showActions(final User u) {
        String[] items = {"Edit name / enable or disable", "Reset password"};
        new MaterialAlertDialogBuilder(this).setTitle(u.fullName).setItems(items, (d, which) -> {
            if (which == 0) {
                showEdit(u);
            } else {
                showReset(u);
            }
        }).show();
    }

    private void showCreate() {
        new FormDialog(this, "New employee")
                .text("name", "Full name", "", FormDialog.TEXT)
                .text("username", "Username (login)", "", FormDialog.PLAIN)
                .text("password", "Password (min 6 characters)", "", FormDialog.PASSWORD)
                .show(f -> vm.create(f.text("username"), f.text("name"), f.text("password"),
                        f.<Long>callback(() -> toast("Employee created."))));
    }

    private void showEdit(final User u) {
        new FormDialog(this, "Edit employee")
                .text("name", "Full name", u.fullName, FormDialog.TEXT)
                .check("active", "Active (can log in)", u.active)
                .show(f -> vm.update(u, f.text("name"), f.checked("active"), f.<Void>callback(null)));
    }

    private void showReset(final User u) {
        new FormDialog(this, "Reset password for " + u.fullName)
                .text("password", "New password (min 6 characters)", "", FormDialog.PASSWORD)
                .positiveLabel("Reset")
                .show(f -> vm.resetPassword(u.id, f.text("password"),
                        f.<Void>callback(() -> toast("Password reset."))));
    }
}
