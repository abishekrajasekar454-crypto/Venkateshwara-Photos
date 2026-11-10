package com.venkateshwaraphotos.shop.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.venkateshwaraphotos.shop.ShopApplication;
import com.venkateshwaraphotos.shop.data.model.PaymentTotal;
import com.venkateshwaraphotos.shop.data.model.SalesSummary;
import com.venkateshwaraphotos.shop.data.model.TopItem;
import com.venkateshwaraphotos.shop.data.repository.ExpenseRepository;
import com.venkateshwaraphotos.shop.data.repository.ReportRepository;
import com.venkateshwaraphotos.shop.util.DateUtil;

import java.util.List;

/** Daily / weekly / monthly report for a movable anchor date. */
public class ReportViewModel extends AndroidViewModel {
    public static final int DAILY = 0;
    public static final int WEEKLY = 1;
    public static final int MONTHLY = 2;

    private int mode = DAILY;
    private long anchor = System.currentTimeMillis();
    private final MutableLiveData<long[]> range = new MutableLiveData<>();
    public final MutableLiveData<String> label = new MutableLiveData<>();
    public final LiveData<SalesSummary> summary;
    public final LiveData<Double> expenses;
    public final LiveData<List<PaymentTotal>> payments;
    public final LiveData<List<TopItem>> top;

    public ReportViewModel(@NonNull Application application) {
        super(application);
        ShopApplication app = (ShopApplication) application;
        final ReportRepository rr = app.reportRepo;
        final ExpenseRepository er = app.expenseRepo;
        summary = Transformations.switchMap(range, r -> rr.observeSummary(r[0], r[1], 0L));
        expenses = Transformations.switchMap(range, r -> er.observeTotal(r[0], r[1]));
        payments = Transformations.switchMap(range, r -> rr.observePayments(r[0], r[1]));
        top = Transformations.switchMap(range, r -> rr.observeTopItems(r[0], r[1]));
        recompute();
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int newMode) {
        mode = newMode;
        recompute();
    }

    public void shift(int dir) {
        if (mode == DAILY) anchor = DateUtil.addDays(anchor, dir);
        else if (mode == WEEKLY) anchor = DateUtil.addDays(anchor, 7 * dir);
        else anchor = DateUtil.addMonths(anchor, dir);
        recompute();
    }

    public void goToToday() {
        anchor = System.currentTimeMillis();
        recompute();
    }

    private void recompute() {
        long from;
        long to;
        String text;
        if (mode == DAILY) {
            from = DateUtil.startOfDay(anchor);
            to = DateUtil.addDays(from, 1);
            text = DateUtil.date(from);
        } else if (mode == WEEKLY) {
            from = DateUtil.startOfWeek(anchor);
            to = DateUtil.addDays(from, 7);
            text = DateUtil.date(from) + " - " + DateUtil.date(to - 1);
        } else {
            from = DateUtil.startOfMonth(anchor);
            to = DateUtil.addMonths(from, 1);
            text = DateUtil.month(from);
        }
        label.setValue(text);
        range.setValue(new long[]{from, to});
    }
}
