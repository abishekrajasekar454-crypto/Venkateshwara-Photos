package com.venkateshwaraphotos.shop.data.model;

import androidx.room.Embedded;

import com.venkateshwaraphotos.shop.data.entity.Sale;

public class SaleRow {
    @Embedded
    public Sale sale;
    public String employeeName;
}
