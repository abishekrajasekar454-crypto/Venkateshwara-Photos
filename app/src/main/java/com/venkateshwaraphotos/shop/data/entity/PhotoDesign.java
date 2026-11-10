package com.venkateshwaraphotos.shop.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "photo_designs",
        foreignKeys = @ForeignKey(entity = Product.class, parentColumns = "id", childColumns = "productId"),
        indices = {@Index(value = {"productId", "name"}, unique = true)})
public class PhotoDesign {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long productId;
    public String name;
    public boolean active = true;
}
