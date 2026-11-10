package com.venkateshwaraphotos.shop.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.venkateshwaraphotos.shop.ShopApplication;
import com.venkateshwaraphotos.shop.data.entity.Category;
import com.venkateshwaraphotos.shop.data.entity.PhotoDesign;
import com.venkateshwaraphotos.shop.data.entity.Product;
import com.venkateshwaraphotos.shop.data.model.DesignStockRow;
import com.venkateshwaraphotos.shop.data.model.VariantInfo;
import com.venkateshwaraphotos.shop.data.repository.CatalogRepository;
import com.venkateshwaraphotos.shop.util.Callback;

import java.util.List;

/** Shared by Category / Product / Variant screens and by the cascading item selector. */
public class CatalogViewModel extends AndroidViewModel {
    private final CatalogRepository repo;
    private final MutableLiveData<Long> categoryFilter = new MutableLiveData<>();
    private final MutableLiveData<Long> productFilter = new MutableLiveData<>();
    public final LiveData<List<Product>> products;
    public final LiveData<List<VariantInfo>> variants;
    public final LiveData<List<PhotoDesign>> designs;

    public CatalogViewModel(@NonNull Application application) {
        super(application);
        final CatalogRepository r = ((ShopApplication) application).catalogRepo;
        repo = r;
        products = Transformations.switchMap(categoryFilter, id -> r.observeProducts(id));
        variants = Transformations.switchMap(productFilter, id -> r.observeVariants(id));
        designs = Transformations.switchMap(productFilter, id -> r.observeDesigns(id));
    }

    public LiveData<List<Category>> categories() {
        return repo.observeCategories();
    }

    public void setCategory(long categoryId) {
        categoryFilter.setValue(categoryId);
    }

    public void setProduct(long productId) {
        productFilter.setValue(productId);
    }

    // ----- writes -----
    public void saveCategory(long id, String name, boolean active, Callback<Long> cb) {
        repo.saveCategory(id, name, active, cb);
    }

    public void saveProduct(long id, long categoryId, String name, String mode, boolean active, Callback<Long> cb) {
        repo.saveProduct(id, categoryId, name, mode, active, cb);
    }

    public void saveVariant(long id, long productId, String name, String size, String frame, double cost,
                            double price, double min, int threshold, boolean active, Callback<Long> cb) {
        repo.saveVariant(id, productId, name, size, frame, cost, price, min, threshold, active, cb);
    }

    public void saveDesign(long id, long productId, String name, boolean active, Callback<Long> cb) {
        repo.saveDesign(id, productId, name, active, cb);
    }

    // ----- one-shot loaders for the cascading selector -----
    public void loadActiveCategories(Callback<List<Category>> cb) {
        repo.loadActiveCategories(cb);
    }

    public void loadActiveProducts(long categoryId, Callback<List<Product>> cb) {
        repo.loadActiveProducts(categoryId, cb);
    }

    public void loadSellableVariants(long productId, Callback<List<VariantInfo>> cb) {
        repo.loadSellableVariants(productId, cb);
    }

    public void loadDesigns(long productId, long variantId, Callback<List<DesignStockRow>> cb) {
        repo.loadDesignStock(productId, variantId, cb);
    }
}
