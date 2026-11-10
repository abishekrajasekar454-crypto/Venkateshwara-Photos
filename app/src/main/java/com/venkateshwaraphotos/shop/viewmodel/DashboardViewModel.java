package com.venkateshwaraphotos.shop.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.venkateshwaraphotos.shop.ShopApplication;
import com.venkateshwaraphotos.shop.data.model.SaleRow;
import com.venkateshwaraphotos.shop.data.model.SalesSummary;
import com.venkateshwaraphotos.shop.data.repository.ExpenseRepository;
import com.venkateshwaraphotos.shop.data.repository.InventoryRepository;
import com.venkateshwaraphotos.shop.data.repository.ReportRepository;
import com.venkateshwaraphotos.shop.data.repository.SalesRepository;
import com.venkateshwaraphotos.shop.util.DateUtil;

import java.util.List;

/**
 * Dashboard figures. Today = sales and gross profit; month-to-date = expenses and net profit.
 * Repositories scope everything to the logged-in employee automatically when the user is not the owner.
 */
public class DashboardViewModel extends AndroidViewModel {
    private final MutableLiveData<long[]> ranges = new MutableLiveData<>();
    public final LiveData<SalesSummary> today;
    public final LiveData<SalesSummary> month;
    public final LiveData<Double> monthExpenses;
    public final LiveData<Integer> units;
    public final LiveData<Double> inventoryValue;
    public final LiveData<Integer> lowCount;
    public final LiveData<List<SaleRow>> recent;

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        ShopApplication app = (ShopApplication) application;
        final ReportRepository rr = app.reportRepo;
        final ExpenseRepository er = app.expenseRepo;
        final InventoryRepository ir = app.inventoryRepo;
        final SalesRepository sr = app.salesRepo;
        today = Transformations.switchMap(ranges, r -> rr.observeSummary(r[0], r[1], 0L));
        month = Transformations.switchMap(ranges, r -> rr.observeSummary(r[2], r[3], 0L));
        monthExpenses = Transformations.switchMap(ranges, r -> er.observeTotal(r[2], r[3]));
        units = ir.observeTotalUnits();
        inventoryValue = ir.observeInventoryValue();
        lowCount = ir.observeLowStockCount();
        recent = sr.observeRecent(5);
        refresh();
    }

    /** Recomputes the day/month window (call from onResume so it rolls over at midnight). */
    public void refresh() {
        long now = System.currentTimeMillis();
        long day = DateUtil.startOfDay(now);
        long monthStart = DateUtil.startOfMonth(now);
        long[] next = new long[]{day, DateUtil.addDays(day, 1), monthStart, DateUtil.addMonths(monthStart, 1)};
        long[] cur = ranges.getValue();
        if (cur == null || cur[0] != next[0] || cur[2] != next[2]) {
            ranges.setValue(next);
        }
    }
}
