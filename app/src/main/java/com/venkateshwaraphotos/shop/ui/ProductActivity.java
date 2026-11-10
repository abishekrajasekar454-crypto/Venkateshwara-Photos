package com.venkateshwaraphotos.shop.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.venkateshwaraphotos.shop.R;
import com.venkateshwaraphotos.shop.data.Const;
import com.venkateshwaraphotos.shop.data.entity.Category;
import com.venkateshwaraphotos.shop.data.entity.Product;
import com.venkateshwaraphotos.shop.ui.adapter.RowAdapter;
import com.venkateshwaraphotos.shop.viewmodel.CatalogViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProductActivity extends BaseActivity {
    private CatalogViewModel vm;
    private List<Category> cats = new ArrayList<>();
    private Category current;
    private Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!guard(true)) return;
        setContentView(R.layout.activity_list);
        setupToolbar("Products");
        vm = new ViewModelProvider(this).get(CatalogViewModel.class);

        LinearLayout filterBar = findViewById(R.id.filterBar);
        filterBar.setVisibility(View.VISIBLE);
        spinner = new Spinner(this);
        filterBar.addView(spinner, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                Ui.dp(this, 48)));

        final View empty = findViewById(R.id.tvEmpty);
        RecyclerView rv = findViewById(R.id.recycler);
        rv.setLayoutManager(new LinearLayoutManager(this));
        final RowAdapter<Product> adapter = new RowAdapter<Product>(p -> new RowAdapter.Row(
                p.name,
                Const.TRACK_DESIGN.equals(p.trackingMode) ? "Stock tracked per photo design" : "Stock tracked per variant",
                null,
                p.active ? "Active" : "Inactive",
                null));
        adapter.onClick(this::showActions);
        rv.setAdapter(adapter);

        vm.categories().observe(this, list -> {
            cats = list;
            long keepId = current == null ? 0 : current.id;
            List<String> names = new ArrayList<>();
            int keep = 0;
            for (int i = 0; i < list.size(); i++) {
                names.add(list.get(i).name + (list.get(i).active ? "" : " (inactive)"));
                if (list.get(i).id == keepId) keep = i;
            }
            spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, names));
            if (!list.isEmpty()) {
                spinner.setSelection(keep);
                current = list.get(keep);
                vm.setCategory(current.id);
            } else {
                current = null;
            }
        });
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < cats.size()) {
                    current = cats.get(position);
                    vm.setCategory(current.id);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        vm.products.observe(this, list -> {
            adapter.setItems(list);
            Ui.showEmpty(empty, list);
        });
        findViewById(R.id.fabAdd).setOnClickListener(v -> {
            if (current == null) {
                toast("Create a category first.");
            } else {
                showForm(null);
            }
        });
    }

    private void showActions(final Product p) {
        final boolean design = Const.TRACK_DESIGN.equals(p.trackingMode);
        String[] items = {"Edit product", design ? "Manage variants & designs" : "Manage variants"};
        new MaterialAlertDialogBuilder(this).setTitle(p.name).setItems(items, (d, which) -> {
            if (which == 0) {
                showForm(p);
            } else {
                startActivity(new Intent(this, VariantActivity.class)
                        .putExtra("productId", p.id)
                        .putExtra("productName", p.name)
                        .putExtra("mode", p.trackingMode));
            }
        }).show();
    }

    private void showForm(final Product p) {
        FormDialog form = new FormDialog(this, p == null ? "New product" : "Edit product");
        form.text("name", "Product name", p == null ? "" : p.name, FormDialog.TEXT);
        if (p == null) {
            form.info("Category: " + current.name);
            form.choice("mode", "Inventory tracking (cannot be changed later)",
                    Arrays.asList("Variant-level stock", "Individual photo-design stock"), 0);
        } else {
            form.info("Inventory tracking: " + (Const.TRACK_DESIGN.equals(p.trackingMode)
                    ? "individual photo-design stock" : "variant-level stock") + " (fixed)");
        }
        form.check("active", "Active", p == null || p.active);
        form.show(f -> {
            String mode = f.choice("mode") == 1 ? Const.TRACK_DESIGN : Const.TRACK_VARIANT;
            vm.saveProduct(p == null ? 0 : p.id, current == null ? 0 : current.id, f.text("name"), mode,
                    f.checked("active"), f.<Long>callback(null));
        });
    }
}
