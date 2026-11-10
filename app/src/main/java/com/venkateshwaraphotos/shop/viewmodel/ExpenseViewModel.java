package com.venkateshwaraphotos.shop.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.venkateshwaraphotos.shop.ShopApplication;
import com.venkateshwaraphotos.shop.data.entity.Expense;
import com.venkateshwaraphotos.shop.data.repository.ExpenseRepository;
import com.venkateshwaraphotos.shop.util.Callback;
import com.venkateshwaraphotos.shop.util.DateUtil;

import java.util.List;

public class ExpenseViewModel extends AndroidViewModel {
    private final ExpenseRepository repo;
    private final MutableLiveData<long[]> range = new MutableLiveData<>();
    private long anchor = System.currentTimeMillis();
    public final LiveData<List<Expense>> expenses;
    public final LiveData<Double> total;

    public ExpenseViewModel(@NonNull Application application) {
        super(application);
        final ExpenseRepository r = ((ShopApplication) application).expenseRepo;
        repo = r;
        expenses = Transformations.switchMap(range, x -> r.observeBetween(x[0], x[1]));
        total = Transformations.switchMap(range, x -> r.observeTotal(x[0], x[1]));
        publish();
    }

    private void publish() {
        long from = DateUtil.startOfMonth(anchor);
        range.setValue(new long[]{from, DateUtil.addMonths(from, 1)});
    }

    public String monthLabel() {
        return DateUtil.month(anchor);
    }

    public void shiftMonth(int dir) {
        anchor = DateUtil.addMonths(anchor, dir);
        publish();
    }

    public void save(long id, String category, String description, double amount, long date, Callback<Long> cb) {
        repo.save(id, category, description, amount, date, cb);
    }

    public void delete(Expense e, Callback<Void> cb) {
        repo.delete(e, cb);
    }
}
