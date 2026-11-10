package com.venkateshwaraphotos.shop.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.venkateshwaraphotos.shop.R;
import com.venkateshwaraphotos.shop.data.Const;
import com.venkateshwaraphotos.shop.data.model.DesignStockRow;
import com.venkateshwaraphotos.shop.data.model.MovementRow;
import com.venkateshwaraphotos.shop.data.model.VariantInfo;
import com.venkateshwaraphotos.shop.ui.adapter.RowAdapter;
import com.venkateshwaraphotos.shop.util.DateUtil;
import com.venkateshwaraphotos.shop.util.Fmt;
import com.venkateshwaraphotos.shop.viewmodel.InventoryViewModel;

import java.util.ArrayList;
import java.util.List;

/** Current stock for everyone (read-only for employees); owner also gets adjustments and movement history. */
public class InventoryActivity extends BaseActivity {
    private InventoryViewModel vm;
    private RecyclerView recycler;
    private View empty;
    private CheckBox cbLow;
    private RowAdapter<VariantInfo> stockAdapter;
    private RowAdapter<MovementRow> moveAdapter;
    private List<VariantInfo> allStock = new ArrayList<>();
    private List<MovementRow> allMoves = new ArrayList<>();
    private boolean showHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!guard(false)) return;
        setContentView(R.layout.activity_list);
        setupToolbar("Inventory");
        vm = new ViewModelProvider(this).get(InventoryViewModel.class);
        final boolean owner = isOwner();

        findViewById(R.id.fabAdd).setVisibility(View.GONE);
        recycler = findViewById(R.id.recycler);
        empty = findViewById(R.id.tvEmpty);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        LinearLayout filterBar = findViewById(R.id.filterBar);
        filterBar.setVisibility(View.VISIBLE);
        View tabs = getLayoutInflater().inflate(R.layout.view_tabs, filterBar, false);
        filterBar.addView(tabs);
        MaterialButton tab1 = tabs.findViewById(R.id.tab1);
        MaterialButton tab2 = tabs.findViewById(R.id.tab2);
        tab1.setText("Stock");
        tab2.setText("History");
        if (!owner) tab2.setVisibility(View.GONE);
        cbLow = tabs.findViewById(R.id.cbOption);
        cbLow.setText("Show low-stock items only");
        cbLow.setVisibility(View.VISIBLE);
        cbLow.setChecked(getIntent().getBooleanExtra("lowOnly", false));
        cbLow.setOnCheckedChangeListener((b, checked) -> render());
        MaterialButtonToggleGroup group = tabs.findViewById(R.id.tabGroup);
        group.check(R.id.tab1);
        group.addOnButtonCheckedListener((g, checkedId, isChecked) -> {
            if (isChecked) {
                showHistory = checkedId == R.id.tab2;
                cbLow.setVisibility(showHistory ? View.GONE : View.VISIBLE);
                render();
            }
        });

        stockAdapter = new RowAdapter<VariantInfo>(v -> {
            String meta = owner
                    ? "Cost " + Fmt.money(v.costPrice) + "  |  Sell " + Fmt.money(v.sellingPrice)
                    : "Sell " + Fmt.money(v.sellingPrice);
            if (Const.TRACK_DESIGN.equals(v.trackingMode)) meta += "  |  per-design stock";
            return new RowAdapter.Row(
                    v.productName + " - " + v.variantName,
                    RowBinders.join("  |  ", v.categoryName, v.size, v.frameType),
                    meta,
                    String.valueOf(v.quantity),
                    v.isLow() ? "LOW (alert at " + v.lowStockThreshold + ")" : "in stock").warn(v.isLow());
        });
        if (owner) stockAdapter.onClick(this::startAdjust);

        moveAdapter = new RowAdapter<MovementRow>(m -> {
            String name = m.productName + " - " + m.variantName
                    + (m.designName != null ? " (" + m.designName + ")" : "");
            int q = m.movement.quantityChange;
            return new RowAdapter.Row(
                    name,
                    DateUtil.dateTime(m.movement.timestamp) + "  |  " + m.userName,
                    RowBinders.join("  |  ", "Balance " + m.movement.balanceAfter, m.movement.note),
                    (q > 0 ? "+" : "") + q,
                    m.movement.type).warn(q < 0);
        });

        vm.stock().observe(this, list -> {
            allStock = list;
            render();
        });
        if (owner) {
            vm.movements().observe(this, list -> {
                allMoves = list;
                render();
            });
        }
        render();
    }

    private void render() {
        if (showHistory) {
            moveAdapter.setItems(allMoves);
            recycler.setAdapter(moveAdapter);
            Ui.showEmpty(empty, allMoves);
        } else {
            List<VariantInfo> shown = new ArrayList<>();
            for (VariantInfo v : allStock) {
                if (!cbLow.isChecked() || (v.isLow() && v.variantActive && v.productActive)) shown.add(v);
            }
            stockAdapter.setItems(shown);
            recycler.setAdapter(stockAdapter);
            Ui.showEmpty(empty, shown);
        }
    }

    private void startAdjust(final VariantInfo v) {
        if (Const.TRACK_DESIGN.equals(v.trackingMode)) {
            vm.loadDesigns(v.productId, v.variantId, Ui.cb(this, (List<DesignStockRow> designs) -> {
                if (designs.isEmpty()) {
                    toast("Add photo designs to this product first (Products > Manage variants & designs).");
                    return;
                }
                showAdjust(v, designs);
            }));
        } else {
            showAdjust(v, null);
        }
    }

    private void showAdjust(final VariantInfo v, final List<DesignStockRow> designs) {
        FormDialog form = new FormDialog(this, "Adjust stock");
        form.info(v.productName + " - " + v.variantName + "\nTotal stock now: " + v.quantity);
        if (designs != null) {
            List<String> names = new ArrayList<>();
            for (DesignStockRow d : designs) names.add(d.designName + "  (" + d.quantity + ")");
            form.choice("design", "Photo design", names, 0);
        }
        form.text("delta", "Change in units (+ add, - remove)", "", FormDialog.SIGNED_INT);
        form.text("note", "Reason (required)", "", FormDialog.SENTENCE);
        form.positiveLabel("Apply");
        form.show(f -> {
            Integer delta = Fmt.parseInt(f.text("delta"));
            if (delta == null || delta == 0) {
                f.error("Enter a whole-number change, e.g. 5 or -2.");
                return;
            }
            Long designId = designs == null ? null : designs.get(f.choice("design")).designId;
            vm.adjust(v.variantId, designId, delta, f.text("note"), f.<Void>callback(() -> toast("Stock adjusted.")));
        });
    }
}
