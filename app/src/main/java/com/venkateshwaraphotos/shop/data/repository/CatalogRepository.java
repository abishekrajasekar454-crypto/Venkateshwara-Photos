package com.venkateshwaraphotos.shop.data.repository;

import androidx.lifecycle.LiveData;

import com.venkateshwaraphotos.shop.data.AppDatabase;
import com.venkateshwaraphotos.shop.data.Const;
import com.venkateshwaraphotos.shop.data.dao.CatalogDao;
import com.venkateshwaraphotos.shop.data.entity.Category;
import com.venkateshwaraphotos.shop.data.entity.PhotoDesign;
import com.venkateshwaraphotos.shop.data.entity.Product;
import com.venkateshwaraphotos.shop.data.entity.Variant;
import com.venkateshwaraphotos.shop.data.entity.VariantStock;
import com.venkateshwaraphotos.shop.data.model.DesignStockRow;
import com.venkateshwaraphotos.shop.data.model.VariantInfo;
import com.venkateshwaraphotos.shop.util.AppExecutors;
import com.venkateshwaraphotos.shop.util.Callback;
import com.venkateshwaraphotos.shop.util.SessionManager;
import com.venkateshwaraphotos.shop.util.ShopException;

import java.util.List;

/** Categories, products, variants and photo designs. Writes are owner-only. */
public class CatalogRepository {
    private final AppDatabase db;
    private final AppExecutors ex;
    private final SessionManager session;

    public CatalogRepository(AppDatabase db, AppExecutors ex, SessionManager session) {
        this.db = db;
        this.ex = ex;
        this.session = session;
    }

    // ----- reads -----
    public LiveData<List<Category>> observeCategories() {
        return db.catalogDao().observeCategories();
    }

    public LiveData<List<Product>> observeProducts(long categoryId) {
        return db.catalogDao().observeProducts(categoryId);
    }

    public LiveData<List<VariantInfo>> observeVariants(long productId) {
        return db.catalogDao().observeVariantInfoByProduct(productId);
    }

    public LiveData<List<PhotoDesign>> observeDesigns(long productId) {
        return db.catalogDao().observeDesigns(productId);
    }

    public void loadActiveCategories(Callback<List<Category>> cb) {
        ex.run(() -> db.catalogDao().activeCategoriesSync(), cb);
    }

    public void loadActiveProducts(final long categoryId, Callback<List<Product>> cb) {
        ex.run(() -> db.catalogDao().activeProductsSync(categoryId), cb);
    }

    /** Cost prices are blanked for non-owners so employees can never see them. */
    public void loadSellableVariants(final long productId, Callback<List<VariantInfo>> cb) {
        ex.run(() -> {
            List<VariantInfo> list = db.catalogDao().sellableVariantsSync(productId);
            if (!session.isOwner()) {
                for (VariantInfo v : list) v.costPrice = 0;
            }
            return list;
        }, cb);
    }

    public void loadDesignStock(final long productId, final long variantId, Callback<List<DesignStockRow>> cb) {
        ex.run(() -> db.catalogDao().designStockSync(productId, variantId), cb);
    }

    // ----- writes (owner only) -----
    public void saveCategory(final long id, final String name, final boolean active, Callback<Long> cb) {
        ex.run(() -> {
            session.requireOwner();
            String n = requireName(name, "Category name");
            CatalogDao d = db.catalogDao();
            long resultId = id;
            if (id == 0) {
                Category c = new Category();
                c.name = n;
                c.active = active;
                resultId = d.insertCategory(c);
            } else {
                Category c = d.findCategory(id);
                if (c == null) throw new ShopException("Category not found.");
                c.name = n;
                c.active = active;
                d.updateCategory(c);
            }
            AuditHelper.log(db, session.getUserId(), "CATEGORY_SAVE", n);
            return resultId;
        }, cb);
    }

    public void saveProduct(final long id, final long categoryId, final String name, final String trackingMode,
                            final boolean active, Callback<Long> cb) {
        ex.run(() -> {
            session.requireOwner();
            String n = requireName(name, "Product name");
            CatalogDao d = db.catalogDao();
            long resultId = id;
            if (id == 0) {
                if (d.findCategory(categoryId) == null) throw new ShopException("Select a category first.");
                if (!Const.TRACK_VARIANT.equals(trackingMode) && !Const.TRACK_DESIGN.equals(trackingMode)) {
                    throw new ShopException("Select an inventory tracking mode.");
                }
                Product p = new Product();
                p.categoryId = categoryId;
                p.name = n;
                p.trackingMode = trackingMode;
                p.active = active;
                resultId = d.insertProduct(p);
            } else {
                Product p = d.findProduct(id);
                if (p == null) throw new ShopException("Product not found.");
                p.name = n; // tracking mode is intentionally immutable
                p.active = active;
                d.updateProduct(p);
            }
            AuditHelper.log(db, session.getUserId(), "PRODUCT_SAVE", n);
            return resultId;
        }, cb);
    }

    public void saveVariant(final long id, final long productId, final String name, final String size,
                            final String frameType, final double cost, final double price, final double minPrice,
                            final int lowStockThreshold, final boolean active, Callback<Long> cb) {
        ex.run(() -> {
            session.requireOwner();
            String n = requireName(name, "Variant name");
            if (cost < 0) throw new ShopException("Cost price cannot be negative.");
            if (price <= 0) throw new ShopException("Selling price must be greater than zero.");
            if (minPrice < 0 || minPrice > price) {
                throw new ShopException("Minimum selling price must be between 0 and the selling price.");
            }
            if (lowStockThreshold < 0) throw new ShopException("Low-stock level cannot be negative.");
            final String sz = size == null ? "" : size.trim();
            final String ft = frameType == null ? "" : frameType.trim();
            return db.runInTransaction(() -> {
                CatalogDao d = db.catalogDao();
                long variantId = id;
                if (id == 0) {
                    if (d.findProduct(productId) == null) throw new ShopException("Product not found.");
                    Variant v = new Variant();
                    v.productId = productId;
                    fill(v, n, sz, ft, cost, price, minPrice, active);
                    variantId = d.insertVariant(v);
                    VariantStock vs = new VariantStock();
                    vs.variantId = variantId;
                    vs.quantity = 0;
                    vs.lowStockThreshold = lowStockThreshold;
                    db.stockDao().insertVariantStock(vs);
                } else {
                    Variant v = d.findVariant(id);
                    if (v == null) throw new ShopException("Variant not found.");
                    fill(v, n, sz, ft, cost, price, minPrice, active);
                    d.updateVariant(v);
                    db.stockDao().updateThreshold(id, lowStockThreshold);
                }
                AuditHelper.log(db, session.getUserId(), "VARIANT_SAVE", n + " sell " + price + " min " + minPrice);
                return variantId;
            });
        }, cb);
    }

    public void saveDesign(final long id, final long productId, final String name, final boolean active, Callback<Long> cb) {
        ex.run(() -> {
            session.requireOwner();
            String n = requireName(name, "Design name");
            CatalogDao d = db.catalogDao();
            long resultId = id;
            if (id == 0) {
                PhotoDesign pd = new PhotoDesign();
                pd.productId = productId;
                pd.name = n;
                pd.active = active;
                resultId = d.insertDesign(pd);
            } else {
                PhotoDesign pd = d.findDesign(id);
                if (pd == null) throw new ShopException("Design not found.");
                pd.name = n;
                pd.active = active;
                d.updateDesign(pd);
            }
            AuditHelper.log(db, session.getUserId(), "DESIGN_SAVE", n);
            return resultId;
        }, cb);
    }

    private static void fill(Variant v, String name, String size, String frame, double cost, double price,
                             double min, boolean active) {
        v.name = name;
        v.size = size;
        v.frameType = frame;
        v.costPrice = cost;
        v.sellingPrice = price;
        v.minSellingPrice = min;
        v.active = active;
    }

    private static String requireName(String s, String label) {
        if (s == null || s.trim().isEmpty()) throw new ShopException(label + " is required.");
        return s.trim();
    }
}
