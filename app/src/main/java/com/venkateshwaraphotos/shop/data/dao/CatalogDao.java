package com.venkateshwaraphotos.shop.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.venkateshwaraphotos.shop.data.entity.Category;
import com.venkateshwaraphotos.shop.data.entity.PhotoDesign;
import com.venkateshwaraphotos.shop.data.entity.Product;
import com.venkateshwaraphotos.shop.data.entity.Variant;
import com.venkateshwaraphotos.shop.data.model.DesignStockRow;
import com.venkateshwaraphotos.shop.data.model.VariantInfo;

import java.util.List;

@Dao
public interface CatalogDao {
    // ----- categories -----
    @Insert
    long insertCategory(Category c);

    @Update
    void updateCategory(Category c);

    @Query("SELECT * FROM categories ORDER BY name")
    LiveData<List<Category>> observeCategories();

    @Query("SELECT * FROM categories WHERE active = 1 ORDER BY name")
    List<Category> activeCategoriesSync();

    @Query("SELECT * FROM categories WHERE id = :id")
    Category findCategory(long id);

    // ----- products -----
    @Insert
    long insertProduct(Product p);

    @Update
    void updateProduct(Product p);

    @Query("SELECT * FROM products WHERE categoryId = :categoryId ORDER BY name")
    LiveData<List<Product>> observeProducts(long categoryId);

    @Query("SELECT * FROM products WHERE categoryId = :categoryId AND active = 1 ORDER BY name")
    List<Product> activeProductsSync(long categoryId);

    @Query("SELECT * FROM products WHERE id = :id")
    Product findProduct(long id);

    // ----- variants -----
    @Insert
    long insertVariant(Variant v);

    @Update
    void updateVariant(Variant v);

    @Query("SELECT * FROM variants WHERE id = :id")
    Variant findVariant(long id);

    @Query(VariantInfo.SELECT + " WHERE v.productId = :productId ORDER BY v.name")
    LiveData<List<VariantInfo>> observeVariantInfoByProduct(long productId);

    @Query(VariantInfo.SELECT + " WHERE v.productId = :productId AND v.active = 1 ORDER BY v.name")
    List<VariantInfo> sellableVariantsSync(long productId);

    @Query(VariantInfo.SELECT + " ORDER BY c.name, p.name, v.name")
    LiveData<List<VariantInfo>> observeAllVariantInfo();

    @Query(VariantInfo.SELECT + " WHERE v.id = :id")
    VariantInfo findVariantInfo(long id);

    // ----- designs -----
    @Insert
    long insertDesign(PhotoDesign d);

    @Update
    void updateDesign(PhotoDesign d);

    @Query("SELECT * FROM photo_designs WHERE id = :id")
    PhotoDesign findDesign(long id);

    @Query("SELECT * FROM photo_designs WHERE productId = :productId ORDER BY name")
    LiveData<List<PhotoDesign>> observeDesigns(long productId);

    @Query("SELECT d.id AS designId, d.name AS designName, COALESCE(ds.quantity, 0) AS quantity "
            + "FROM photo_designs d LEFT JOIN design_stock ds ON ds.designId = d.id AND ds.variantId = :variantId "
            + "WHERE d.productId = :productId AND d.active = 1 ORDER BY d.name")
    List<DesignStockRow> designStockSync(long productId, long variantId);
}
