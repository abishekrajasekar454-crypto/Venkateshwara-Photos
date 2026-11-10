package com.venkateshwaraphotos.shop.data.repository;

import androidx.lifecycle.LiveData;

import com.venkateshwaraphotos.shop.data.AppDatabase;
import com.venkateshwaraphotos.shop.data.model.AuditRow;
import com.venkateshwaraphotos.shop.data.model.PaymentTotal;
import com.venkateshwaraphotos.shop.data.model.SalesSummary;
import com.venkateshwaraphotos.shop.data.model.TopItem;
import com.venkateshwaraphotos.shop.util.AppExecutors;
import com.venkateshwaraphotos.shop.util.Callback;
import com.venkateshwaraphotos.shop.util.SessionManager;

import java.util.List;

/**
 * Aggregates for dashboards and reports. Gross profit = revenue - COGS (from sale-time snapshots).
 * Net profit = gross profit - expenses. Purchases are NOT expenses (that would double count COGS).
 */
public class ReportRepository {
    private final AppDatabase db;
    private final AppExecutors ex;
    private final SessionManager session;

    public ReportRepository(AppDatabase db, AppExecutors ex, SessionManager session) {
        this.db = db;
        this.ex = ex;
        this.session = session;
    }

    /** employeeId = 0 for the whole shop (owner). Employees are always scoped to themselves. */
    public LiveData<SalesSummary> observeSummary(long fromTs, long toTs, long employeeId) {
        long scope = session.isOwner() ? employeeId : session.getUserId();
        return db.saleDao().observeSummary(fromTs, toTs, scope);
    }

    public LiveData<List<PaymentTotal>> observePayments(long fromTs, long toTs) {
        return db.saleDao().observePayments(fromTs, toTs);
    }

    public LiveData<List<TopItem>> observeTopItems(long fromTs, long toTs) {
        return db.saleDao().observeTopItems(fromTs, toTs);
    }

    public void recentAudit(Callback<List<AuditRow>> cb) {
        ex.run(() -> {
            session.requireOwner();
            return db.auditDao().recentSync();
        }, cb);
    }
}
