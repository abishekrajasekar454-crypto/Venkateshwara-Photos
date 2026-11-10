package com.venkateshwaraphotos.shop.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.venkateshwaraphotos.shop.data.entity.DesignStock;
import com.venkateshwaraphotos.shop.data.entity.StockMovement;
import com.venkateshwaraphotos.shop.data.entity.VariantStock;
import com.venkateshwaraphotos.shop.data.model.MovementRow;

import java.util.List;

@Dao
public interface StockDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertVariantStock(VariantStock s);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertDesignStockIfMissing(DesignStock s);

    /** Returns rows changed; 0 means the change would make stock negative (or the row is missing). */
    @Query("UPDATE variant_stock SET quantity = quantity + :delta WHERE variantId = :variantId AND quantity + :delta >= 0")
    int changeVariantStock(long variantId, int delta);

    @Query("UPDATE design_stock SET quantity = quantity + :delta WHERE designId = :designId AND variantId = :variantId AND quantity + :delta >= 0")
    int changeDesignStock(long designId, long variantId, int delta);

    @Query("SELECT quantity FROM variant_stock WHERE variantId = :variantId")
    Integer variantQty(long variantId);

    @Query("SELECT quantity FROM design_stock WHERE designId = :designId AND variantId = :variantId")
    Integer designQty(long designId, long variantId);

    @Query("UPDATE variant_stock SET lowStockThreshold = :threshold WHERE variantId = :variantId")
    void updateThreshold(long variantId, int threshold);

    @Insert
    long insertMovement(StockMovement m);

    @Query("SELECT m.*, p.name AS productName, v.name AS variantName, d.name AS designName, u.fullName AS userName "
            + "FROM stock_movements m JOIN variants v ON v.id = m.variantId JOIN products p ON p.id = v.productId "
            + "LEFT JOIN photo_designs d ON d.id = m.designId LEFT JOIN users u ON u.id = m.userId "
            + "ORDER BY m.timestamp DESC, m.id DESC LIMIT 300")
    LiveData<List<MovementRow>> observeMovements();

    @Query("SELECT COALESCE(SUM(vs.quantity), 0) FROM variant_stock vs JOIN variants v ON v.id = vs.variantId WHERE v.active = 1")
    LiveData<Integer> observeTotalUnits();

    @Query("SELECT COALESCE(SUM(vs.quantity * v.costPrice), 0) FROM variant_stock vs JOIN variants v ON v.id = vs.variantId WHERE v.active = 1")
    LiveData<Double> observeInventoryValue();

    @Query("SELECT COUNT(*) FROM variant_stock vs JOIN variants v ON v.id = vs.variantId WHERE v.active = 1 AND vs.quantity <= vs.lowStockThreshold")
    LiveData<Integer> observeLowStockCount();
}
