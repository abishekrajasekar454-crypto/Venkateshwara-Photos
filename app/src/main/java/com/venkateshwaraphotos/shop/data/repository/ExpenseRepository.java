package com.venkateshwaraphotos.shop.data.repository;

import androidx.lifecycle.LiveData;

import com.venkateshwaraphotos.shop.data.AppDatabase;
import com.venkateshwaraphotos.shop.data.entity.Expense;
import com.venkateshwaraphotos.shop.util.AppExecutors;
import com.venkateshwaraphotos.shop.util.Callback;
import com.venkateshwaraphotos.shop.util.Fmt;
import com.venkateshwaraphotos.shop.util.SessionManager;
import com.venkateshwaraphotos.shop.util.ShopException;

import java.util.List;

public class ExpenseRepository {
    private final AppDatabase db;
    private final AppExecutors ex;
    private final SessionManager session;

    public ExpenseRepository(AppDatabase db, AppExecutors ex, SessionManager session) {
        this.db = db;
        this.ex = ex;
        this.session = session;
    }

    public LiveData<List<Expense>> observeBetween(long fromTs, long toTs) {
        return db.expenseDao().observeBetween(fromTs, toTs);
    }

    public LiveData<Double> observeTotal(long fromTs, long toTs) {
        return db.expenseDao().observeTotal(fromTs, toTs);
    }

    public void save(final long id, final String category, final String description, final double amount,
                     final long date, Callback<Long> cb) {
        ex.run(() -> {
            session.requireOwner();
            if (category == null || category.trim().isEmpty()) throw new ShopException("Expense category is required.");
            if (amount <= 0) throw new ShopException("Amount must be greater than zero.");
            Expense e = new Expense();
            e.id = id;
            e.category = category.trim();
            e.description = description == null ? "" : description.trim();
            e.amount = Fmt.round2(amount);
            e.date = date;
            e.createdBy = session.getUserId();
            long resultId = id;
            if (id == 0) {
                resultId = db.expenseDao().insert(e);
            } else {
                db.expenseDao().update(e);
            }
            AuditHelper.log(db, e.createdBy, "EXPENSE_SAVE", e.category + " " + Fmt.money(e.amount));
            return resultId;
        }, cb);
    }

    public void delete(final Expense e, Callback<Void> cb) {
        ex.run(() -> {
            session.requireOwner();
            db.expenseDao().delete(e);
            AuditHelper.log(db, session.getUserId(), "EXPENSE_DELETE", e.category + " " + Fmt.money(e.amount));
            return null;
        }, cb);
    }
}
