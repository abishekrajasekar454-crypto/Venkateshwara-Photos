package com.venkateshwaraphotos.shop.data.repository;

import com.venkateshwaraphotos.shop.data.AppDatabase;
import com.venkateshwaraphotos.shop.data.Const;
import com.venkateshwaraphotos.shop.data.entity.PhotoDesign;
import com.venkateshwaraphotos.shop.data.entity.Purchase;
import com.venkateshwaraphotos.shop.data.entity.PurchaseItem;
import com.venkateshwaraphotos.shop.data.model.PurchaseLine;
import com.venkateshwaraphotos.shop.data.model.VariantInfo;
import com.venkateshwaraphotos.shop.util.AppExecutors;
import com.venkateshwaraphotos.shop.util.Callback;
import com.venkateshwaraphotos.shop.util.Fmt;
import com.venkateshwaraphotos.shop.util.SessionManager;
import com.venkateshwaraphotos.shop.util.ShopException;

import java.util.ArrayList;
import java.util.List;

public class PurchaseRepository {
    private final AppDatabase db;
    private final AppExecutors ex;
    private final SessionManager session;

    public PurchaseRepository(AppDatabase db, AppExecutors ex, SessionManager session) {
        this.db = db;
        this.ex = ex;
        this.session = session;
    }

    public void recent(Callback<List<Purchase>> cb) {
        ex.run(() -> {
            session.requireOwner();
            return db.purchaseDao().recentSync();
        }, cb);
    }

    /** Owner only. Purchase header + items + stock increase + stock movements in one transaction. */
    public void save(final String supplier, final long date, final List<PurchaseLine> lines, Callback<Long> cb) {
        final List<PurchaseLine> copy = new ArrayList<>(lines);
        ex.run(() -> {
            session.requireOwner();
            if (supplier == null || supplier.trim().isEmpty()) throw new ShopException("Supplier name is required.");
            if (copy.isEmpty()) throw new ShopException("Add at least one item.");
            final long uid = session.getUserId();
            final long now = System.currentTimeMillis();
            return db.runInTransaction(() -> {
                Purchase p = new Purchase();
                p.supplierName = supplier.trim();
                p.purchaseDate = date;
                p.createdBy = uid;
                p.createdAt = now;
                long purchaseId = db.purchaseDao().insertPurchase(p);
                double total = 0;
                for (PurchaseLine l : copy) {
                    if (l.quantity <= 0) throw new ShopException("Quantity must be at least 1.");
                    if (l.unitCost < 0) throw new ShopException("Cost cannot be negative.");
                    VariantInfo vi = db.catalogDao().findVariantInfo(l.variantId);
                    if (vi == null) throw new ShopException("Variant not found.");
                    Long designId = null;
                    if (Const.TRACK_DESIGN.equals(vi.trackingMode)) {
                        if (l.designId == null) throw new ShopException("Select a design for " + vi.productName + ".");
                        PhotoDesign d = db.catalogDao().findDesign(l.designId);
                        if (d == null || d.productId != vi.productId) throw new ShopException("Invalid design.");
                        designId = d.id;
                    }
                    PurchaseItem it = new PurchaseItem();
                    it.purchaseId = purchaseId;
                    it.variantId = l.variantId;
                    it.designId = designId;
                    it.quantity = l.quantity;
                    it.unitCost = l.unitCost;
                    it.lineTotal = Fmt.round2(l.quantity * l.unitCost);
                    db.purchaseDao().insertItem(it);
                    StockOps.apply(db, l.variantId, designId, l.quantity, Const.MOVE_PURCHASE, purchaseId,
                            "Purchase from " + supplier.trim(), uid, now);
                    total += it.lineTotal;
                }
                db.purchaseDao().updateTotal(purchaseId, Fmt.round2(total));
                AuditHelper.log(db, uid, "PURCHASE", "Purchase #" + purchaseId + " from " + supplier.trim()
                        + " total " + Fmt.money(total));
                return purchaseId;
            });
        }, cb);
    }
}
