package com.venkateshwaraphotos.shop.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "stock_movements",
        foreignKeys = {
                @ForeignKey(entity = Variant.class, parentColumns = "id", childColumns = "variantId"),
                @ForeignKey(entity = User.class, parentColumns = "id", childColumns = "userId")},
        indices = {@Index("variantId"), @Index("userId"), @Index("timestamp")})
public class StockMovement {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long variantId;
    public Long designId;
    /** PURCHASE, SALE, ADJUSTMENT or OPENING. */
    public String type;
    /** Positive = stock in, negative = stock out. */
    public int quantityChange;
    public int balanceAfter;
    /** Sale id / purchase id (0 when not applicable). */
    public long referenceId;
    public String note;
    public long timestamp;
    public long userId;
}
