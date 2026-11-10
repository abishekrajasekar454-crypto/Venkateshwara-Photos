package com.venkateshwaraphotos.shop.ui;

import android.os.Bundle;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.venkateshwaraphotos.shop.R;
import com.venkateshwaraphotos.shop.data.entity.SaleItem;
import com.venkateshwaraphotos.shop.ui.adapter.RowAdapter;
import com.venkateshwaraphotos.shop.util.DateUtil;
import com.venkateshwaraphotos.shop.util.Fmt;
import com.venkateshwaraphotos.shop.viewmodel.HistoryViewModel;

public class SaleDetailActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!guard(false)) return;
        final long saleId = getIntent().getLongExtra("saleId", 0);
        if (saleId == 0) {
            finish();
            return;
        }
        setContentView(R.layout.activity_sale_detail);
        setupToolbar("Sale #" + saleId);
        final boolean owner = isOwner();
        HistoryViewModel vm = new ViewModelProvider(this).get(HistoryViewModel.class);
        final TextView header = findViewById(R.id.tvHeader);
        final TextView footer = findViewById(R.id.tvFooter);

        RecyclerView rv = findViewById(R.id.recycler);
        rv.setLayoutManager(new LinearLayoutManager(this));
        final RowAdapter<SaleItem> adapter = new RowAdapter<SaleItem>(i -> new RowAdapter.Row(
                i.productName + " - " + i.variantName + (i.designName != null ? " (" + i.designName + ")" : ""),
                i.quantity + " x " + Fmt.money(i.unitPrice),
                owner ? "Cost " + Fmt.money(i.unitCost) + " each  |  Profit " + Fmt.money((i.unitPrice - i.unitCost) * i.quantity) : null,
                Fmt.money(i.unitPrice * i.quantity),
                null));
        rv.setAdapter(adapter);

        vm.sale(saleId).observe(this, row -> {
            if (row == null) return;
            if (!owner && row.sale.employeeId != app().session.getUserId()) {
                toast("You can only view your own sales.");
                finish();
                return;
            }
            header.setText(DateUtil.dateTime(row.sale.timestamp) + "\nSold by: " + row.employeeName
                    + "\nPayment: " + row.sale.paymentMode + "  |  " + row.sale.itemCount + " unit(s)");
            String text = "Total: " + Fmt.money(row.sale.totalAmount);
            if (owner) {
                text += "\nCost: " + Fmt.money(row.sale.totalCost) + "   Gross profit: "
                        + Fmt.money(row.sale.totalAmount - row.sale.totalCost);
            }
            footer.setText(text);
        });
        vm.items(saleId).observe(this, adapter::setItems);
    }
}
