package com.venkateshwaraphotos.shop.ui;

import com.venkateshwaraphotos.shop.data.model.SaleRow;
import com.venkateshwaraphotos.shop.ui.adapter.RowAdapter;
import com.venkateshwaraphotos.shop.util.DateUtil;
import com.venkateshwaraphotos.shop.util.Fmt;

/** Row formatting shared by more than one screen. */
public final class RowBinders {
    private RowBinders() {}

    public static RowAdapter.Row sale(SaleRow r, boolean showProfit) {
        String profit = showProfit ? "Profit " + Fmt.money(r.sale.totalAmount - r.sale.totalCost) : "";
        return new RowAdapter.Row(
                "Sale #" + r.sale.id,
                DateUtil.dateTime(r.sale.timestamp) + "  |  " + r.employeeName,
                r.sale.paymentMode + "  |  " + r.sale.itemCount + " item(s)",
                Fmt.money(r.sale.totalAmount),
                profit);
    }

    public static String join(String sep, String... parts) {
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p == null || p.trim().isEmpty()) continue;
            if (sb.length() > 0) sb.append(sep);
            sb.append(p.trim());
        }
        return sb.toString();
    }
}
