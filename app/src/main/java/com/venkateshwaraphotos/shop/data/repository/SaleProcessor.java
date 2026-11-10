package com.venkateshwaraphotos.shop.data.repository;

import com.venkateshwaraphotos.shop.data.AppDatabase;
import com.venkateshwaraphotos.shop.data.Const;
import com.venkateshwaraphotos.shop.data.dao.CatalogDao;
import com.venkateshwaraphotos.shop.data.dao.SaleDao;
import com.venkateshwaraphotos.shop.data.dao.StockDao;
import com.venkateshwaraphotos.shop.data.entity.PhotoDesign;
import com.venkateshwaraphotos.shop.data.entity.Sale;
import com.venkateshwaraphotos.shop.data.entity.SaleItem;
import com.venkateshwaraphotos.shop.data.model.CartLine;
import com.venkateshwaraphotos.shop.data.model.VariantInfo;
import com.venkateshwaraphotos.shop.util.Fmt;
import com.venkateshwaraphotos.shop.util.ShopException;

import java.util.List;

/**
 * Records a sale: sale header, sale items (with price/cost snapshots), stock deduction and stock
 * movements all happen inside ONE Room transaction. Any failure rolls everything back.
 */
public final class SaleProcessor {
    private SaleProcessor() {}

    public static long process(final AppDatabase db, final long employeeId, final List<CartLine> lines,
                               final String payment, final long timestamp) {
        if (lines == null || lines.isEmpty()) {
            throw new ShopException("The cart is empty.");
        }
        if (!Const.PAY_CASH.equals(payment) && !Const.PAY_UPI.equals(payment) && !Const.PAY_OTHER.equals(payment)) {
            throw new ShopException("Select a payment method.");
        }
        return db.runInTransaction(() -> {
            CatalogDao cd = db.catalogDao();
            StockDao sd = db.stockDao();
            SaleDao sa = db.saleDao();

            Sale sale = new Sale();
            sale.employeeId = employeeId;
            sale.timestamp = timestamp;
            sale.paymentMode = payment;
            long saleId = sa.insertSale(sale);

            double total = 0;
            double cost = 0;
            int units = 0;
            for (CartLine l : lines) {
                VariantInfo vi = cd.findVariantInfo(l.variantId);
                if (vi == null || !vi.variantActive || !vi.productActive || !vi.categoryActive) {
                    throw new ShopException("'" + l.displayName() + "' is no longer available for sale.");
                }
                if (l.quantity <= 0) {
                    throw new ShopException("Quantity must be at least 1.");
                }
                if (l.unitPrice <= 0) {
                    throw new ShopException("Enter a valid price for " + l.displayName() + ".");
                }
                if (l.unitPrice + 0.0001 < vi.minSellingPrice) {
                    throw new ShopException("Price for " + vi.productName + " - " + vi.variantName
                            + " is below the minimum of " + Fmt.money(vi.minSellingPrice) + ".");
                }
                Long designId = null;
                String designName = null;
                if (Const.TRACK_DESIGN.equals(vi.trackingMode)) {
                    if (l.designId == null) {
                        throw new ShopException("Select a design for " + vi.productName + ".");
                    }
                    PhotoDesign d = cd.findDesign(l.designId);
                    if (d == null || !d.active || d.productId != vi.productId) {
                        throw new ShopException("The selected design is not available.");
                    }
                    designId = d.id;
                    designName = d.name;
                }
                Integer available = designId != null ? sd.designQty(designId, l.variantId) : sd.variantQty(l.variantId);
                int have = available == null ? 0 : available;
                if (have < l.quantity) {
                    throw new ShopException("Insufficient stock for " + l.displayName() + ". Available: " + have + ".");
                }
                StockOps.apply(db, l.variantId, designId, -l.quantity, Const.MOVE_SALE, saleId,
                        "Sale #" + saleId, employeeId, timestamp);

                SaleItem it = new SaleItem();
                it.saleId = saleId;
                it.variantId = l.variantId;
                it.designId = designId;
                it.productName = vi.productName;
                it.variantName = vi.variantName;
                it.designName = designName;
                it.quantity = l.quantity;
                it.unitPrice = l.unitPrice;
                it.unitCost = vi.costPrice; // historical cost snapshot
                sa.insertItem(it);

                total += l.unitPrice * l.quantity;
                cost += vi.costPrice * l.quantity;
                units += l.quantity;
            }
            sa.updateTotals(saleId, Fmt.round2(total), Fmt.round2(cost), units);
            AuditHelper.log(db, employeeId, "SALE", "Sale #" + saleId + " total " + Fmt.money(total));
            return saleId;
        });
    }
}
