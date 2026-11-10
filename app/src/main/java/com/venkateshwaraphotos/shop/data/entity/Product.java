package com.venkateshwaraphotos.shop.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "products",
        foreignKeys = @ForeignKey(entity = Category.class, parentColumns = "id", childColumns = "categoryId"),
        indices = {@Index(value = {"categoryId", "name"}, unique = true)})
public class Product {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long categoryId;
    public String name;
    /** VARIANT or DESIGN (see Const). Fixed at creation. */
    public String trackingMode;
    public boolean active = true;
}
