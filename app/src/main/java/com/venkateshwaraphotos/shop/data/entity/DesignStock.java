package com.venkateshwaraphotos.shop.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(tableName = "design_stock",
        primaryKeys = {"designId", "variantId"},
        foreignKeys = {
                @ForeignKey(entity = PhotoDesign.class, parentColumns = "id", childColumns = "designId",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Variant.class, parentColumns = "id", childColumns = "variantId",
                        onDelete = ForeignKey.CASCADE)},
        indices = {@Index("variantId")})
public class DesignStock {
    public long designId;
    public long variantId;
    public int quantity;
}
