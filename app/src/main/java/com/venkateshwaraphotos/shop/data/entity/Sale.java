package com.venkateshwaraphotos.shop.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "sales",
        foreignKeys = @ForeignKey(entity = User.class, parentColumns = "id", childColumns = "employeeId"),
        indices = {@Index("employeeId"), @Index("timestamp")})
public class Sale {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long employeeId;
    public long timestamp;
    public double totalAmount;
    /** Total cost of goods sold (snapshot). */
    public double totalCost;
    public int itemCount;
    public String paymentMode;
}
