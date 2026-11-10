package com.venkateshwaraphotos.shop.data.model;

import java.util.Objects;

/** One line in the New Sale cart. designId is null for variant-tracked products. */
public class CartLine {
    public long variantId;
    public Long designId;
    public String productName;
    public String variantName;
    public String designName;
    public double unitPrice;
    public double minPrice;
    public int quantity;

    public double lineTotal() {
        return unitPrice * quantity;
    }

    public String displayName() {
        String s = productName + " - " + variantName;
        if (designName != null) s += " (" + designName + ")";
        return s;
    }

    public boolean sameItem(CartLine o) {
        return variantId == o.variantId && Objects.equals(designId, o.designId);
    }
}
