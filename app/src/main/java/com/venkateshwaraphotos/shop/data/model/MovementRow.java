package com.venkateshwaraphotos.shop.data.model;

import androidx.room.Embedded;

import com.venkateshwaraphotos.shop.data.entity.StockMovement;

public class MovementRow {
    @Embedded
    public StockMovement movement;
    public String productName;
    public String variantName;
    public String designName;
    public String userName;
}
