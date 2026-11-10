package com.venkateshwaraphotos.shop.data.model;

public class SalesSummary {
    public double revenue;
    public double cogs;
    public int saleCount;

    public double gross() {
        return revenue - cogs;
    }
}
