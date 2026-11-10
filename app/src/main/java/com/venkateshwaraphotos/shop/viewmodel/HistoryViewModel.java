package com.venkateshwaraphotos.shop.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.venkateshwaraphotos.shop.ShopApplication;
import com.venkateshwaraphotos.shop.data.entity.SaleItem;
import com.venkateshwaraphotos.shop.data.entity.User;
import com.venkateshwaraphotos.shop.data.model.SaleRow;
import com.venkateshwaraphotos.shop.data.repository.SalesRepository;
import com.venkateshwaraphotos.shop.data.repository.UserRepository;

import java.util.List;

public class HistoryViewModel extends AndroidViewModel {
    private final SalesRepository sales;
    private final UserRepository users;
    private final MutableLiveData<long[]> filter = new MutableLiveData<>();
    public final LiveData<List<SaleRow>> list;

    public HistoryViewModel(@NonNull Application application) {
        super(application);
        ShopApplication app = (ShopApplication) application;
        final SalesRepository s = app.salesRepo;
        sales = s;
        users = app.userRepo;
        list = Transformations.switchMap(filter, f -> s.observeSales(f[0], f[1], f[2]));
    }

    /** toExclusive is the first millisecond AFTER the range; employeeId 0 = everyone. */
    public void setFilter(long from, long toExclusive, long employeeId) {
        filter.setValue(new long[]{from, toExclusive, employeeId});
    }

    public LiveData<List<User>> users() {
        return users.observeAllUsers();
    }

    public LiveData<SaleRow> sale(long id) {
        return sales.observeSale(id);
    }

    public LiveData<List<SaleItem>> items(long saleId) {
        return sales.observeItems(saleId);
    }
}
