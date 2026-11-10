package com.venkateshwaraphotos.shop.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.venkateshwaraphotos.shop.ShopApplication;
import com.venkateshwaraphotos.shop.data.entity.Purchase;
import com.venkateshwaraphotos.shop.data.model.PurchaseLine;
import com.venkateshwaraphotos.shop.util.Callback;

import java.util.ArrayList;
import java.util.List;

public class PurchaseViewModel extends AndroidViewModel {
    private final ShopApplication app;
    private final MutableLiveData<List<PurchaseLine>> lines = new MutableLiveData<>(new ArrayList<PurchaseLine>());

    public PurchaseViewModel(@NonNull Application application) {
        super(application);
        app = (ShopApplication) application;
    }

    public LiveData<List<PurchaseLine>> getLines() {
        return lines;
    }

    public static double totalOf(List<PurchaseLine> list) {
        double t = 0;
        if (list != null) {
            for (PurchaseLine l : list) t += l.lineTotal();
        }
        return t;
    }

    public void addLine(PurchaseLine l) {
        List<PurchaseLine> cur = new ArrayList<>(lines.getValue());
        cur.add(l);
        lines.setValue(cur);
    }

    public void removeLine(PurchaseLine l) {
        List<PurchaseLine> cur = new ArrayList<>(lines.getValue());
        cur.remove(l);
        lines.setValue(cur);
    }

    public void save(String supplier, long date, final Callback<Long> cb) {
        app.purchaseRepo.save(supplier, date, lines.getValue(), new Callback<Long>() {
            @Override
            public void onSuccess(Long id) {
                lines.setValue(new ArrayList<PurchaseLine>());
                cb.onSuccess(id);
            }

            @Override
            public void onError(String message) {
                cb.onError(message);
            }
        });
    }

    public void recent(Callback<List<Purchase>> cb) {
        app.purchaseRepo.recent(cb);
    }
}
