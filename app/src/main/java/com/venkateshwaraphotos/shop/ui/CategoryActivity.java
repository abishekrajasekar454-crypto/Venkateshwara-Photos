package com.venkateshwaraphotos.shop.ui;

import android.os.Bundle;
import android.view.View;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.venkateshwaraphotos.shop.R;
import com.venkateshwaraphotos.shop.data.entity.Category;
import com.venkateshwaraphotos.shop.ui.adapter.RowAdapter;
import com.venkateshwaraphotos.shop.viewmodel.CatalogViewModel;

public class CategoryActivity extends BaseActivity {
    private CatalogViewModel vm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!guard(true)) return;
        setContentView(R.layout.activity_list);
        setupToolbar("Categories");
        vm = new ViewModelProvider(this).get(CatalogViewModel.class);

        final View empty = findViewById(R.id.tvEmpty);
        RecyclerView rv = findViewById(R.id.recycler);
        rv.setLayoutManager(new LinearLayoutManager(this));
        final RowAdapter<Category> adapter = new RowAdapter<Category>(c -> new RowAdapter.Row(
                c.name, c.active ? "Active" : "Inactive (hidden from sales)", null, null, null));
        adapter.onClick(this::showForm);
        rv.setAdapter(adapter);

        vm.categories().observe(this, list -> {
            adapter.setItems(list);
            Ui.showEmpty(empty, list);
        });
        findViewById(R.id.fabAdd).setOnClickListener(v -> showForm(null));
    }

    private void showForm(final Category c) {
        new FormDialog(this, c == null ? "New category" : "Edit category")
                .text("name", "Category name", c == null ? "" : c.name, FormDialog.TEXT)
                .check("active", "Active", c == null || c.active)
                .show(f -> vm.saveCategory(c == null ? 0 : c.id, f.text("name"), f.checked("active"),
                        f.<Long>callback(null)));
    }
}
