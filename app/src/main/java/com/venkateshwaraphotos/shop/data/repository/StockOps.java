package com.venkateshwaraphotos.shop.data.repository;

import com.venkateshwaraphotos.shop.data.AppDatabase;
import com.venkateshwaraphotos.shop.data.dao.StockDao;
import com.venkateshwaraphotos.shop.data.entity.DesignStock;
import com.venkateshwaraphotos.shop.data.entity.StockMovement;
import com.venkateshwaraphotos.shop.util.ShopException;

/**
 * The single place where stock changes. Must be called inside a Room transaction.
 * Keeps VariantStock (total) and DesignStock (per design) consistent and writes the movement record.
 */
public final class StockOps {
    private StockOps() {}

    public static void apply(AppDatabase db, long variantId, Long designId, int delta, String type,
                             long referenceId, String note, long userId, long timestamp) {
        StockDao sd = db.stockDao();
        if (designId != null) {
            DesignStock row = new DesignStock();
            row.designId = designId;
            row.variantId = variantId;
            row.quantity = 0;
            sd.insertDesignStockIfMissing(row);
            if (sd.changeDesignStock(designId, variantId, delta) == 0) {
                throw new ShopException("Not enough stock for this design.");
            }
        }
        if (sd.changeVariantStock(variantId, delta) == 0) {
            throw new ShopException("Not enough stock.");
        }
        Integer balance = designId != null ? sd.designQty(designId, variantId) : sd.variantQty(variantId);
        StockMovement m = new StockMovement();
        m.variantId = variantId;
        m.designId = designId;
        m.type = type;
        m.quantityChange = delta;
        m.balanceAfter = balance == null ? 0 : balance;
        m.referenceId = referenceId;
        m.note = note;
        m.timestamp = timestamp;
        m.userId = userId;
        sd.insertMovement(m);
    }
}
