package com.venkateshwaraphotos.shop.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.venkateshwaraphotos.shop.R;
import com.venkateshwaraphotos.shop.data.entity.Purchase;
import com.venkateshwaraphotos.shop.data.model.DesignStockRow;
import com.venkateshwaraphotos.shop.data.model.PurchaseLine;
import com.venkateshwaraphotos.shop.data.model.VariantInfo;
import com.venkateshwaraphotos.shop.ui.adapter.RowAdapter;
import com.venkateshwaraphotos.shop.util.Callback;
import com.venkateshwaraphotos.shop.util.DateUtil;
import com.venkateshwaraphotos.shop.util.Fmt;
import com.venkateshwaraphotos.shop.viewmodel.CatalogViewModel;
import com.venkateshwaraphotos.shop.viewmodel.PurchaseViewModel;

import java.util.List;

/** Owner only: record incoming stock. Saving increases stock and writes stock movements atomically. */
public class PurchaseActivity extends BaseActivity {
    private PurchaseViewModel vm;
    private CatalogViewModel catalogVm;
    private MaterialButton btnDate;
    private long purchaseDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!guard(true)) return;
        setContentView(R.layout.activity_purchase);
        MaterialToolbar tb = setupToolbar("Record purchase");
        tb.inflateMenu(R.menu.menu_purchase);
        tb.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_history) showHistory();
            return true;
        });
        vm = new ViewModelProvider(this).get(PurchaseViewModel.class);
        catalogVm = new ViewModelProvider(this).get(CatalogViewModel.class);

        final EditText etSupplier = findViewById(R.id.etSupplier);
        final TextView tvTotal = findViewById(R.id.tvTotal);
        final View empty = findViewById(R.id.tvEmpty);
        final MaterialButton btnSave = findViewById(R.id.btnSave);
        btnDate = findViewById(R.id.btnDate);
        purchaseDate = DateUtil.startOfDay(System.currentTimeMillis());
        btnDate.setText("Date: " + DateUtil.date(purchaseDate));
        btnDate.setOnClickListener(v -> Ui.pickDate(this, purchaseDate, ts -> {
            purchaseDate = ts;
            btnDate.setText("Date: " + DateUtil.date(ts));
        }));

        RecyclerView rv = findViewById(R.id.recycler);
        rv.setLayoutManager(new LinearLayoutManager(this));
        final RowAdapter<PurchaseLine> adapter = new RowAdapter<PurchaseLine>(l -> new RowAdapter.Row(
                l.displayName(),
                l.quantity + " x " + Fmt.money(l.unitCost),
                null,
                Fmt.money(l.lineTotal()),
                null));
        adapter.onClick(l -> Ui.confirm(this, "Remove item", "Remove " + l.displayName() + "?",
                () -> vm.removeLine(l)));
        rv.setAdapter(adapter);

        vm.getLines().observe(this, list -> {
            adapter.setItems(list);
            Ui.showEmpty(empty, list);
            tvTotal.setText("Total: " + Fmt.money(PurchaseViewModel.totalOf(list)));
        });

        findViewById(R.id.btnAddItem).setOnClickListener(v -> showAddItem());
        btnSave.setOnClickListener(v -> {
            btnSave.setEnabled(false);
            vm.save(etSupplier.getText().toString(), purchaseDate, new Callback<Long>() {
                @Override
                public void onSuccess(Long id) {
                    btnSave.setEnabled(true);
                    etSupplier.setText("");
                    toast("Purchase #" + id + " saved. Stock updated.");
                }

                @Override
                public void onError(String message) {
                    btnSave.setEnabled(true);
                    toast(message);
                }
            });
        });
    }

    private void showAddItem() {
        final View view = getLayoutInflater().inflate(R.layout.dialog_purchase_item, null);
        final EditText etQty = view.findViewById(R.id.etItemQty);
        final EditText etCost = view.findViewById(R.id.etItemCost);
        final TextView tvErr = view.findViewById(R.id.tvItemError);
        final ItemSelector[] holder = new ItemSelector[1];
        final long[] lastVariant = new long[]{0};
        holder[0] = new ItemSelector(this, view.findViewById(R.id.itemSelector), catalogVm, () -> {
            ItemSelector s = holder[0];
            if (s == null) return;
            VariantInfo v = s.getVariant();
            if (v != null && v.variantId != lastVariant[0]) {
                lastVariant[0] = v.variantId;
                etCost.setText(Fmt.plain(v.costPrice));
            }
        });

        final AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle("Add purchase item")
                .setView(view)
                .setPositiveButton("Add", null)
                .setNegativeButton("Cancel", null)
                .create();
        dialog.setOnDismissListener(d -> holder[0].dispose());
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            ItemSelector s = holder[0];
            VariantInfo vi = s.getVariant();
            Integer qty = Fmt.parseInt(etQty.getText().toString());
            Double cost = Fmt.parseDouble(etCost.getText().toString());
            String error = null;
            if (vi == null || !s.isComplete()) {
                error = s.needsDesign() ? "Select a photo design." : "Select a category, product and variant.";
            } else if (qty == null || qty <= 0) {
                error = "Enter a quantity of at least 1.";
            } else if (cost == null || cost < 0) {
                error = "Enter a valid cost per unit.";
            }
            if (error != null) {
                tvErr.setText(error);
                tvErr.setVisibility(View.VISIBLE);
                return;
            }
            DesignStockRow design = s.needsDesign() ? s.getDesign() : null;
            PurchaseLine line = new PurchaseLine();
            line.variantId = vi.variantId;
            line.designId = design == null ? null : design.designId;
            line.productName = vi.productName;
            line.variantName = vi.variantName;
            line.designName = design == null ? null : design.designName;
            line.quantity = qty;
            line.unitCost = cost;
            vm.addLine(line);
            dialog.dismiss();
        }));
        dialog.show();
    }

    private void showHistory() {
        vm.recent(Ui.cb(this, (List<Purchase> list) -> {
            StringBuilder sb = new StringBuilder();
            for (Purchase p : list) {
                sb.append("#").append(p.id).append("  ").append(DateUtil.date(p.purchaseDate)).append("\n")
                        .append(p.supplierName).append("  -  ").append(Fmt.money(p.totalAmount)).append("\n\n");
            }
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Recent purchases")
                    .setMessage(sb.length() == 0 ? "No purchases recorded yet." : sb.toString())
                    .setPositiveButton("Close", null)
                    .show();
        }));
    }
}
