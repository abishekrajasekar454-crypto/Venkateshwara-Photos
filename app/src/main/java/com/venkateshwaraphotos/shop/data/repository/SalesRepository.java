package com.venkateshwaraphotos.shop.data.repository;

import androidx.lifecycle.LiveData;

import com.venkateshwaraphotos.shop.data.AppDatabase;
import com.venkateshwaraphotos.shop.data.entity.SaleItem;
import com.venkateshwaraphotos.shop.data.model.CartLine;
import com.venkateshwaraphotos.shop.data.model.SaleRow;
import com.venkateshwaraphotos.shop.util.AppExecutors;
import com.venkateshwaraphotos.shop.util.Callback;
import com.venkateshwaraphotos.shop.util.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class SalesRepository {
    private final AppDatabase db;
    private final AppExecutors ex;
    private final SessionManager session;

    public SalesRepository(AppDatabase db, AppExecutors ex, SessionManager session) {
        this.db = db;
        this.ex = ex;
        this.session = session;
    }

    /** Employees are always restricted to their own sales, whatever filter is passed in. */
    public LiveData<List<SaleRow>> observeSales(long fromTs, long toTs, long employeeId) {
        long scope = session.isOwner() ? employeeId : session.getUserId();
        return db.saleDao().observeSales(fromTs, toTs, scope);
    }

    public LiveData<List<SaleRow>> observeRecent(int maxRows) {
        long scope = session.isOwner() ? 0L : session.getUserId();
        return db.saleDao().observeRecent(scope, maxRows);
    }

    public LiveData<SaleRow> observeSale(long id) {
        return db.saleDao().observeSale(id);
    }

    public LiveData<List<SaleItem>> observeItems(long saleId) {
        return db.saleDao().observeItems(saleId);
    }

    public void completeSale(final List<CartLine> lines, final String payment, Callback<Long> cb) {
        final List<CartLine> copy = new ArrayList<>(lines);
        ex.run(() -> {
            session.requireLogin();
            return SaleProcessor.process(db, session.getUserId(), copy, payment, System.currentTimeMillis());
        }, cb);
    }
}
