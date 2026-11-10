package com.venkateshwaraphotos.shop.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "sale_items",
        foreignKeys = {
                @ForeignKey(entity = Sale.class, parentColumns = "id", childColumns = "saleId",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Variant.class, parentColumns = "id", childColumns = "variantId")},
        indices = {@Index("saleId"), @Index("variantId")})
public class SaleItem {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long saleId;
    public long variantId;
    public Long designId;
    // Historical snapshots - never change after the sale.
    public String productName;
    public String variantName;
    public String designName;
    public int quantity;
    public double unitPrice;
    public double unitCost;
}
