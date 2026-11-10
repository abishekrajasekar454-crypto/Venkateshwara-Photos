package com.venkateshwaraphotos.shop.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "expenses",
        foreignKeys = @ForeignKey(entity = User.class, parentColumns = "id", childColumns = "createdBy"),
        indices = {@Index("createdBy"), @Index("date")})
public class Expense {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String category;
    public String description;
    public double amount;
    public long date;
    public long createdBy;
}
