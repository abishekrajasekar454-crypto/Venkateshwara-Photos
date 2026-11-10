package com.venkateshwaraphotos.shop.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.venkateshwaraphotos.shop.data.entity.Purchase;
import com.venkateshwaraphotos.shop.data.entity.PurchaseItem;

import java.util.List;

@Dao
public interface PurchaseDao {
    @Insert
    long insertPurchase(Purchase p);

    @Insert
    void insertItem(PurchaseItem i);

    @Query("UPDATE purchases SET totalAmount = :total WHERE id = :id")
    void updateTotal(long id, double total);

    @Query("SELECT * FROM purchases ORDER BY purchaseDate DESC, id DESC LIMIT 50")
    List<Purchase> recentSync();
}
