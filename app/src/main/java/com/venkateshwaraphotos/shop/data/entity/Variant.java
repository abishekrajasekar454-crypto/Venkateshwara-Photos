package com.venkateshwaraphotos.shop.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "variants",
        foreignKeys = @ForeignKey(entity = Product.class, parentColumns = "id", childColumns = "productId"),
        indices = {@Index(value = {"productId", "name"}, unique = true)})
public class Variant {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long productId;
    public String name;
    public String size;
    public String frameType;
    public double costPrice;
    public double sellingPrice;
    public double minSellingPrice;
    public boolean active = true;
}
