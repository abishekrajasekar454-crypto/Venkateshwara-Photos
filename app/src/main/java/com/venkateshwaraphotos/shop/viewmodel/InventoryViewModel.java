package com.venkateshwaraphotos.shop.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.venkateshwaraphotos.shop.ShopApplication;
import com.venkateshwaraphotos.shop.data.model.DesignStockRow;
import com.venkateshwaraphotos.shop.data.model.MovementRow;
import com.venkateshwaraphotos.shop.data.model.VariantInfo;
import com.venkateshwaraphotos.shop.data.repository.CatalogRepository;
import com.venkateshwaraphotos.shop.data.repository.InventoryRepository;
import com.venkateshwaraphotos.shop.util.Callback;

import java.util.List;

public class InventoryViewModel extends AndroidViewModel {
    private final InventoryRepository inventory;
    private final CatalogRepository catalog;

    public InventoryViewModel(@NonNull Application application) {
        super(application);
        ShopApplication app = (ShopApplication) application;
        inventory = app.inventoryRepo;
        catalog = app.catalogRepo;
    }

    public LiveData<List<VariantInfo>> stock() {
        return inventory.observeStock();
    }

    public LiveData<List<MovementRow>> movements() {
        return inventory.observeMovements();
    }

    public void loadDesigns(long productId, long variantId, Callback<List<DesignStockRow>> cb) {
        catalog.loadDesignStock(productId, variantId, cb);
    }

    public void adjust(long variantId, Long designId, int delta, String note, Callback<Void> cb) {
        inventory.adjust(variantId, designId, delta, note, cb);
    }
}
