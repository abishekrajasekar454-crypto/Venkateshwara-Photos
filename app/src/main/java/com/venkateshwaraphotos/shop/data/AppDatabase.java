package com.venkateshwaraphotos.shop.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.venkateshwaraphotos.shop.data.dao.AuditDao;
import com.venkateshwaraphotos.shop.data.dao.CatalogDao;
import com.venkateshwaraphotos.shop.data.dao.ExpenseDao;
import com.venkateshwaraphotos.shop.data.dao.PurchaseDao;
import com.venkateshwaraphotos.shop.data.dao.SaleDao;
import com.venkateshwaraphotos.shop.data.dao.StockDao;
import com.venkateshwaraphotos.shop.data.dao.UserDao;
import com.venkateshwaraphotos.shop.data.entity.AuditLog;
import com.venkateshwaraphotos.shop.data.entity.Category;
import com.venkateshwaraphotos.shop.data.entity.DesignStock;
import com.venkateshwaraphotos.shop.data.entity.Expense;
import com.venkateshwaraphotos.shop.data.entity.PhotoDesign;
import com.venkateshwaraphotos.shop.data.entity.Product;
import com.venkateshwaraphotos.shop.data.entity.Purchase;
import com.venkateshwaraphotos.shop.data.entity.PurchaseItem;
import com.venkateshwaraphotos.shop.data.entity.Sale;
import com.venkateshwaraphotos.shop.data.entity.SaleItem;
import com.venkateshwaraphotos.shop.data.entity.StockMovement;
import com.venkateshwaraphotos.shop.data.entity.User;
import com.venkateshwaraphotos.shop.data.entity.Variant;
import com.venkateshwaraphotos.shop.data.entity.VariantStock;

@Database(entities = {
        User.class, Category.class, Product.class, Variant.class, PhotoDesign.class,
        VariantStock.class, DesignStock.class, Sale.class, SaleItem.class,
        Purchase.class, PurchaseItem.class, Expense.class, StockMovement.class, AuditLog.class
}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UserDao userDao();

    public abstract CatalogDao catalogDao();

    public abstract StockDao stockDao();

    public abstract SaleDao saleDao();

    public abstract PurchaseDao purchaseDao();

    public abstract ExpenseDao expenseDao();

    public abstract AuditDao auditDao();

    private static volatile AppDatabase instance;

    public static AppDatabase get(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "venkateshwara_photos.db").build();
                }
            }
        }
        return instance;
    }
}
