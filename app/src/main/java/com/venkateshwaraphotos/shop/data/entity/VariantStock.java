package com.venkateshwaraphotos.shop.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

/** Total stock for a variant. For design-tracked products this equals the sum of its DesignStock rows. */
@Entity(tableName = "variant_stock",
        foreignKeys = @ForeignKey(entity = Variant.class, parentColumns = "id", childColumns = "variantId",
                onDelete = ForeignKey.CASCADE))
public class VariantStock {
    @PrimaryKey
    public long variantId;
    public int quantity;
    public int lowStockThreshold = 5;
}
