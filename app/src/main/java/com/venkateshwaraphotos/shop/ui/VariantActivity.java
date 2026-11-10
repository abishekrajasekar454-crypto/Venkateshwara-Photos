package com.venkateshwaraphotos.shop.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.venkateshwaraphotos.shop.R;
import com.venkateshwaraphotos.shop.data.Const;
import com.venkateshwaraphotos.shop.data.entity.PhotoDesign;
import com.venkateshwaraphotos.shop.data.model.VariantInfo;
import com.venkateshwaraphotos.shop.ui.adapter.RowAdapter;
import com.venkateshwaraphotos.shop.util.Fmt;
import com.venkateshwaraphotos.shop.viewmodel.CatalogViewModel;

import java.util.List;

/** Manage the variants (and, for design-tracked products, the photo designs) of one product. Owner only. */
public class VariantActivity extends BaseActivity {
    private CatalogViewModel vm;
    private long productId;
    private boolean showDesigns;
    private RecyclerView recycler;
    private View empty;
    private RowAdapter<VariantInfo> variantAdapter;
    private RowAdapter<PhotoDesign> designAdapter;
    private List<VariantInfo> variantList;
    private List<PhotoDesign> designList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!guard(true)) return;
        productId = getIntent().getLongExtra("productId", 0);
        String productName = getIntent().getStringExtra("productName");
        boolean designMode = Const.TRACK_DESIGN.equals(getIntent().getStringExtra("mode"));
        if (productId == 0) {
            finish();
            return;
        }
        setContentView(R.layout.activity_list);
        setupToolbar(productName == null ? "Variants" : productName);
        vm = new ViewModelProvider(this).get(CatalogViewModel.class);
        vm.setProduct(productId);

        recycler = findViewById(R.id.recycler);
        empty = findViewById(R.id.tvEmpty);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        variantAdapter = new RowAdapter<VariantInfo>(v -> new RowAdapter.Row(
                v.variantName,
                RowBinders.join("  |  ", v.size, v.frameType),
                "Cost " + Fmt.money(v.costPrice) + "  |  Sell " + Fmt.money(v.sellingPrice)
                        + "  |  Min " + Fmt.money(v.minSellingPrice) + "  |  ID " + v.variantId,
                v.quantity + " in stock",
                v.variantActive ? "" : "Inactive").warn(v.isLow()));
        variantAdapter.onClick(this::showVariantForm);

        designAdapter = new RowAdapter<PhotoDesign>(d -> new RowAdapter.Row(
                d.name, null, null, d.active ? "Active" : "Inactive", null));
        designAdapter.onClick(this::showDesignForm);

        if (designMode) {
            LinearLayout filterBar = findViewById(R.id.filterBar);
            filterBar.setVisibility(View.VISIBLE);
            View tabs = getLayoutInflater().inflate(R.layout.view_tabs, filterBar, false);
            filterBar.addView(tabs);
            ((MaterialButton) tabs.findViewById(R.id.tab1)).setText("Variants");
            ((MaterialButton) tabs.findViewById(R.id.tab2)).setText("Designs");
            MaterialButtonToggleGroup group = tabs.findViewById(R.id.tabGroup);
            group.check(R.id.tab1);
            group.addOnButtonCheckedListener((g, checkedId, isChecked) -> {
                if (isChecked) {
                    showDesigns = checkedId == R.id.tab2;
                    render();
                }
            });
        }

        vm.variants.observe(this, list -> {
            variantList = list;
            variantAdapter.setItems(list);
            render();
        });
        vm.designs.observe(this, list -> {
            designList = list;
            designAdapter.setItems(list);
            render();
        });
        findViewById(R.id.fabAdd).setOnClickListener(v -> {
            if (showDesigns) {
                showDesignForm(null);
            } else {
                showVariantForm(null);
            }
        });
        render();
    }

    private void render() {
        recycler.setAdapter(showDesigns ? designAdapter : variantAdapter);
        Ui.showEmpty(empty, showDesigns ? designList : variantList);
    }

    private void showVariantForm(final VariantInfo v) {
        new FormDialog(this, v == null ? "New variant" : "Edit variant")
                .text("name", "Variant name (e.g. Small - Premium Frame)", v == null ? "" : v.variantName, FormDialog.TEXT)
                .text("size", "Size", v == null ? "" : v.size, FormDialog.TEXT)
                .text("frame", "Frame type", v == null ? "" : v.frameType, FormDialog.TEXT)
                .text("cost", "Cost price (₹)", v == null ? "" : Fmt.plain(v.costPrice), FormDialog.DECIMAL)
                .text("price", "Selling price (₹)", v == null ? "" : Fmt.plain(v.sellingPrice), FormDialog.DECIMAL)
                .text("min", "Minimum selling price (₹)", v == null ? "" : Fmt.plain(v.minSellingPrice), FormDialog.DECIMAL)
                .text("low", "Low-stock alert at (units)",
                        String.valueOf(v == null ? Const.DEFAULT_LOW_STOCK : v.lowStockThreshold), FormDialog.INTEGER)
                .check("active", "Active (available for sale)", v == null || v.variantActive)
                .info(v == null ? "Stock starts at 0 - add it with a Purchase." : "Stock: " + v.quantity
                        + " (change via Purchases or Inventory adjustments).")
                .show(f -> {
                    Double cost = Fmt.parseDouble(f.text("cost"));
                    Double price = Fmt.parseDouble(f.text("price"));
                    Double min = Fmt.parseDouble(f.text("min"));
                    Integer low = Fmt.parseInt(f.text("low"));
                    if (cost == null || price == null || min == null || low == null) {
                        f.error("Enter valid numbers for the prices and the low-stock level.");
                        return;
                    }
                    vm.saveVariant(v == null ? 0 : v.variantId, productId, f.text("name"), f.text("size"),
                            f.text("frame"), cost, price, min, low, f.checked("active"), f.<Long>callback(null));
                });
    }

    private void showDesignForm(final PhotoDesign d) {
        new FormDialog(this, d == null ? "New photo design" : "Edit photo design")
                .text("name", "Design name", d == null ? "" : d.name, FormDialog.TEXT)
                .check("active", "Active", d == null || d.active)
                .show(f -> vm.saveDesign(d == null ? 0 : d.id, productId, f.text("name"), f.checked("active"),
                        f.<Long>callback(null)));
    }
}
