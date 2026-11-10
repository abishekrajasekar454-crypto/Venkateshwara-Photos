package com.venkateshwaraphotos.shop.data.model;

/** A variant joined with its product, category and current stock. */
public class VariantInfo {
    public static final String SELECT = "SELECT v.id AS variantId, v.productId AS productId, "
            + "v.name AS variantName, v.size AS size, v.frameType AS frameType, v.costPrice AS costPrice, "
            + "v.sellingPrice AS sellingPrice, v.minSellingPrice AS minSellingPrice, v.active AS variantActive, "
            + "p.name AS productName, p.trackingMode AS trackingMode, p.active AS productActive, "
            + "c.id AS categoryId, c.name AS categoryName, c.active AS categoryActive, "
            + "COALESCE(vs.quantity, 0) AS quantity, COALESCE(vs.lowStockThreshold, 5) AS lowStockThreshold "
            + "FROM variants v JOIN products p ON p.id = v.productId JOIN categories c ON c.id = p.categoryId "
            + "LEFT JOIN variant_stock vs ON vs.variantId = v.id";

    public long variantId;
    public long productId;
    public String variantName;
    public String size;
    public String frameType;
    public double costPrice;
    public double sellingPrice;
    public double minSellingPrice;
    public boolean variantActive;
    public String productName;
    public String trackingMode;
    public boolean productActive;
    public long categoryId;
    public String categoryName;
    public boolean categoryActive;
    public int quantity;
    public int lowStockThreshold;

    public boolean isLow() {
        return quantity <= lowStockThreshold;
    }
}
