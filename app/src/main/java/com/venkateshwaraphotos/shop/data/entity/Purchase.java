package com.venkateshwaraphotos.shop.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "purchases",
        foreignKeys = @ForeignKey(entity = User.class, parentColumns = "id", childColumns = "createdBy"),
        indices = {@Index("createdBy"), @Index("purchaseDate")})
public class Purchase {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String supplierName;
    public long purchaseDate;
    public double totalAmount;
    public long createdBy;
    public long createdAt;
}
