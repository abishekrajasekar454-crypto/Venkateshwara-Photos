package com.venkateshwaraphotos.shop.data.model;

public class PurchaseLine {
    public long variantId;
    public Long designId;
    public String productName;
    public String variantName;
    public String designName;
    public int quantity;
    public double unitCost;

    public double lineTotal() {
        return quantity * unitCost;
    }

    public String displayName() {
        String s = productName + " - " + variantName;
        if (designName != null) s += " (" + designName + ")";
        return s;
    }
}
