package com.venkateshwaraphotos.shop.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "purchase_items",
        foreignKeys = {
                @ForeignKey(entity = Purchase.class, parentColumns = "id", childColumns = "purchaseId",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Variant.class, parentColumns = "id", childColumns = "variantId")},
        indices = {@Index("purchaseId"), @Index("variantId")})
public class PurchaseItem {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long purchaseId;
    public long variantId;
    public Long designId;
    public int quantity;
    public double unitCost;
    public double lineTotal;
}
