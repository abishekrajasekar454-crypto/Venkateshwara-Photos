package com.venkateshwaraphotos.shop;

import android.app.Application;

import com.venkateshwaraphotos.shop.data.AppDatabase;
import com.venkateshwaraphotos.shop.data.repository.CatalogRepository;
import com.venkateshwaraphotos.shop.data.repository.ExpenseRepository;
import com.venkateshwaraphotos.shop.data.repository.InventoryRepository;
import com.venkateshwaraphotos.shop.data.repository.PurchaseRepository;
import com.venkateshwaraphotos.shop.data.repository.ReportRepository;
import com.venkateshwaraphotos.shop.data.repository.SalesRepository;
import com.venkateshwaraphotos.shop.data.repository.SeedData;
import com.venkateshwaraphotos.shop.data.repository.UserRepository;
import com.venkateshwaraphotos.shop.util.AppExecutors;
import com.venkateshwaraphotos.shop.util.SessionManager;

/** Application-scoped singletons (simple manual dependency wiring). */
public class ShopApplication extends Application {
    public AppDatabase db;
    public AppExecutors executors;
    public SessionManager session;
    public UserRepository userRepo;
    public CatalogRepository catalogRepo;
    public InventoryRepository inventoryRepo;
    public SalesRepository salesRepo;
    public PurchaseRepository purchaseRepo;
    public ExpenseRepository expenseRepo;
    public ReportRepository reportRepo;

    @Override
    public void onCreate() {
        super.onCreate();
        db = AppDatabase.get(this);
        executors = new AppExecutors();
        session = new SessionManager(this);
        userRepo = new UserRepository(db, executors, session);
        catalogRepo = new CatalogRepository(db, executors, session);
        inventoryRepo = new InventoryRepository(db, executors, session);
        salesRepo = new SalesRepository(db, executors, session);
        purchaseRepo = new PurchaseRepository(db, executors, session);
        expenseRepo = new ExpenseRepository(db, executors, session);
        reportRepo = new ReportRepository(db, executors, session);
        // Seeding runs on the same single-thread executor, so a login attempt always queues behind it.
        executors.diskIO.execute(() -> SeedData.seedIfEmpty(db));
    }
}
