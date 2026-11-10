package com.venkateshwaraphotos.shop.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.venkateshwaraphotos.shop.data.AppDatabase;
import com.venkateshwaraphotos.shop.data.Const;
import com.venkateshwaraphotos.shop.data.model.MovementRow;
import com.venkateshwaraphotos.shop.data.model.VariantInfo;
import com.venkateshwaraphotos.shop.util.AppExecutors;
import com.venkateshwaraphotos.shop.util.Callback;
import com.venkateshwaraphotos.shop.util.SessionManager;
import com.venkateshwaraphotos.shop.util.ShopException;

import java.util.List;

public class InventoryRepository {
    private final AppDatabase db;
    private final AppExecutors ex;
    private final SessionManager session;

    public InventoryRepository(AppDatabase db, AppExecutors ex, SessionManager session) {
        this.db = db;
        this.ex = ex;
        this.session = session;
    }

    /** Cost prices are blanked for non-owners so employees never receive them. */
    public LiveData<List<VariantInfo>> observeStock() {
        LiveData<List<VariantInfo>> source = db.catalogDao().observeAllVariantInfo();
        if (session.isOwner()) {
            return source;
        }
        return Transformations.map(source, list -> {
            for (VariantInfo v : list) {
                v.costPrice = 0;
            }
            return list;
        });
    }

    public LiveData<List<MovementRow>> observeMovements() {
        return db.stockDao().observeMovements();
    }

    public LiveData<Integer> observeTotalUnits() {
        return db.stockDao().observeTotalUnits();
    }

    public LiveData<Double> observeInventoryValue() {
        return db.stockDao().observeInventoryValue();
    }

    public LiveData<Integer> observeLowStockCount() {
        return db.stockDao().observeLowStockCount();
    }

    /** Owner-authorised manual adjustment (positive or negative delta) with a mandatory reason. */
    public void adjust(final long variantId, final Long designId, final int delta, final String note, Callback<Void> cb) {
        ex.run(() -> {
            session.requireOwner();
            if (delta == 0) throw new ShopException("Enter a non-zero quantity change.");
            if (note == null || note.trim().isEmpty()) throw new ShopException("A reason is required for adjustments.");
            db.runInTransaction(() -> {
                VariantInfo vi = db.catalogDao().findVariantInfo(variantId);
                if (vi == null) throw new ShopException("Variant not found.");
                Long d = null;
                if (Const.TRACK_DESIGN.equals(vi.trackingMode)) {
                    if (designId == null) throw new ShopException("Select a design.");
                    d = designId;
                }
                StockOps.apply(db, variantId, d, delta, Const.MOVE_ADJUST, 0, note.trim(),
                        session.getUserId(), System.currentTimeMillis());
                AuditHelper.log(db, session.getUserId(), "STOCK_ADJUST",
                        vi.productName + " - " + vi.variantName + " " + (delta > 0 ? "+" : "") + delta + ": " + note.trim());
            });
            return null;
        }, cb);
    }
}
