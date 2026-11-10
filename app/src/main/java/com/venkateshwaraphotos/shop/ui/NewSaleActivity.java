package com.venkateshwaraphotos.shop.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.venkateshwaraphotos.shop.R;
import com.venkateshwaraphotos.shop.data.Const;
import com.venkateshwaraphotos.shop.data.model.CartLine;
import com.venkateshwaraphotos.shop.data.model.DesignStockRow;
import com.venkateshwaraphotos.shop.data.model.VariantInfo;
import com.venkateshwaraphotos.shop.ui.adapter.RowAdapter;
import com.venkateshwaraphotos.shop.util.Callback;
import com.venkateshwaraphotos.shop.util.Fmt;
import com.venkateshwaraphotos.shop.viewmodel.CatalogViewModel;
import com.venkateshwaraphotos.shop.viewmodel.SaleViewModel;

/**
 * Point-of-sale screen: pick category > product > variant (> design), set quantity (and price within the
 * minimum limit), add to cart, choose payment, complete. No bill is printed or generated.
 */
public class NewSaleActivity extends BaseActivity {
    private SaleViewModel saleVm;
    private ItemSelector selector;
    private EditText etPrice;
    private EditText etQty;
    private TextView tvInfo;
    private long pricedVariantId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!guard(false)) return;
        setContentView(R.layout.activity_new_sale);
        setupToolbar("New Sale");
        saleVm = new ViewModelProvider(this).get(SaleViewModel.class);
        CatalogViewModel catalogVm = new ViewModelProvider(this).get(CatalogViewModel.class);

        etPrice = findViewById(R.id.etPrice);
        etQty = findViewById(R.id.etQty);
        tvInfo = findViewById(R.id.tvInfo);
        final TextView tvTotal = findViewById(R.id.tvTotal);
        final View empty = findViewById(R.id.tvEmpty);
        final RadioGroup rg = findViewById(R.id.rgPayment);
        final MaterialButton btnComplete = findViewById(R.id.btnComplete);

        selector = new ItemSelector(this, findViewById(R.id.itemSelector), catalogVm, this::onSelectionChanged);
        tvInfo.setText("Choose a category, product and variant.");

        RecyclerView rv = findViewById(R.id.recycler);
        rv.setLayoutManager(new LinearLayoutManager(this));
        final RowAdapter<CartLine> cartAdapter = new RowAdapter<CartLine>(l -> new RowAdapter.Row(
                l.displayName(),
                l.quantity + " x " + Fmt.money(l.unitPrice),
                null,
                Fmt.money(l.lineTotal()),
                null));
        cartAdapter.onClick(l -> Ui.confirm(this, "Remove item", "Remove " + l.displayName() + " from the cart?",
                () -> saleVm.removeLine(l)));
        rv.setAdapter(cartAdapter);

        saleVm.getCart().observe(this, list -> {
            cartAdapter.setItems(list);
            Ui.showEmpty(empty, list);
            tvTotal.setText("Total: " + Fmt.money(SaleViewModel.totalOf(list)));
        });

        findViewById(R.id.btnAddToCart).setOnClickListener(v -> addToCart());

        btnComplete.setOnClickListener(v -> {
            if (saleVm.getCart().getValue() == null || saleVm.getCart().getValue().isEmpty()) {
                toast("Add at least one item to the cart.");
                return;
            }
            int checked = rg.getCheckedRadioButtonId();
            String payment = checked == R.id.rbUpi ? Const.PAY_UPI
                    : checked == R.id.rbOther ? Const.PAY_OTHER : Const.PAY_CASH;
            btnComplete.setEnabled(false);
            saleVm.completeSale(payment, new Callback<Long>() {
                @Override
                public void onSuccess(Long saleId) {
                    btnComplete.setEnabled(true);
                    toast("Sale #" + saleId + " recorded.");
                    selector.refreshStock();
                }

                @Override
                public void onError(String message) {
                    btnComplete.setEnabled(true);
                    toast(message);
                }
            });
        });
    }

    private void onSelectionChanged() {
        if (tvInfo == null) return;
        VariantInfo v = selector.getVariant();
        if (v == null) {
            tvInfo.setText("Choose a category, product and variant.");
            pricedVariantId = 0;
            return;
        }
        if (pricedVariantId != v.variantId) {
            etPrice.setText(Fmt.plain(v.sellingPrice));
            pricedVariantId = v.variantId;
        }
        String info = "In stock: " + selector.availableStock() + "   |   Minimum price: " + Fmt.money(v.minSellingPrice);
        if (selector.needsDesign() && selector.getDesign() == null) {
            info = "Select a photo design.   |   Minimum price: " + Fmt.money(v.minSellingPrice);
        }
        tvInfo.setText(info);
    }

    private void addToCart() {
        VariantInfo v = selector.getVariant();
        if (v == null || !selector.isComplete()) {
            toast(v != null && selector.needsDesign() ? "Select a photo design." : "Select a category, product and variant.");
            return;
        }
        Integer qty = Fmt.parseInt(etQty.getText().toString());
        Double price = Fmt.parseDouble(etPrice.getText().toString());
        if (qty == null || price == null) {
            toast("Enter a valid quantity and price.");
            return;
        }
        DesignStockRow d = selector.needsDesign() ? selector.getDesign() : null;
        CartLine line = new CartLine();
        line.variantId = v.variantId;
        line.designId = d == null ? null : d.designId;
        line.productName = v.productName;
        line.variantName = v.variantName;
        line.designName = d == null ? null : d.designName;
        line.unitPrice = price;
        line.minPrice = v.minSellingPrice;
        line.quantity = qty;
        String error = saleVm.addLine(line, selector.availableStock());
        if (error != null) {
            toast(error);
        } else {
            etQty.setText("1");
            toast("Added to cart.");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (selector != null) selector.dispose();
    }
}
