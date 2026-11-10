package com.venkateshwaraphotos.shop.data.repository;

import com.venkateshwaraphotos.shop.data.AppDatabase;
import com.venkateshwaraphotos.shop.data.Const;
import com.venkateshwaraphotos.shop.data.dao.CatalogDao;
import com.venkateshwaraphotos.shop.data.entity.Category;
import com.venkateshwaraphotos.shop.data.entity.Expense;
import com.venkateshwaraphotos.shop.data.entity.PhotoDesign;
import com.venkateshwaraphotos.shop.data.entity.Product;
import com.venkateshwaraphotos.shop.data.entity.User;
import com.venkateshwaraphotos.shop.data.entity.Variant;
import com.venkateshwaraphotos.shop.data.entity.VariantStock;
import com.venkateshwaraphotos.shop.data.model.CartLine;
import com.venkateshwaraphotos.shop.util.DateUtil;
import com.venkateshwaraphotos.shop.util.PasswordUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Sample data for testing, inserted once on first launch (when the users table is empty).
 * Default logins (CHANGE THEM before real use): owner / owner123, ramesh / emp12345, suresh / emp12345.
 */
public final class SeedData {
    private SeedData() {}

    public static void seedIfEmpty(final AppDatabase db) {
        if (db.userDao().count() > 0) return;
        db.runInTransaction(() -> {
            final long now = System.currentTimeMillis();
            long ownerId = addUser(db, "owner", "Shop Owner", "owner123", Const.OWNER, now);
            long ramesh = addUser(db, "ramesh", "Ramesh", "emp12345", Const.EMPLOYEE, now);
            addUser(db, "suresh", "Suresh", "emp12345", Const.EMPLOYEE, now);

            CatalogDao cd = db.catalogDao();
            long photos = cat(cd, "Photos");
            long prasad = cat(cd, "Prasad Items");
            long albums = cat(cd, "Albums");
            long acc = cat(cd, "Accessories");
            long printing = cat(cd, "Printing Services");

            // Variant-tracked product with frame variants
            long frame = prod(cd, photos, "Photo Frame", Const.TRACK_VARIANT);
            long fSN = var(db, frame, "Small - Normal Frame", "Small", "Normal", 80, 150, 130, 5);
            long fSP = var(db, frame, "Small - Premium Frame", "Small", "Premium", 140, 250, 220, 5);
            long fSG = var(db, frame, "Small - Glass Frame", "Small", "Glass", 170, 300, 270, 5);
            long fMN = var(db, frame, "Medium - Normal Frame", "Medium", "Normal", 160, 280, 250, 5);
            long fMP = var(db, frame, "Medium - Premium Frame", "Medium", "Premium", 260, 450, 400, 5);
            long fLW = var(db, frame, "Large - Wooden Frame", "Large", "Wooden", 520, 850, 780, 3);

            // Design-tracked product
            long deity = prod(cd, photos, "Deity Photo", Const.TRACK_DESIGN);
            long dV = design(cd, deity, "Lord Venkateshwara");
            long dG = design(cd, deity, "Ganesha");
            long dL = design(cd, deity, "Lakshmi");
            long dM = design(cd, deity, "Murugan");
            long d8 = var(db, deity, "8x10 Print", "8x10 in", "Unframed", 30, 70, 60, 5);
            long d12 = var(db, deity, "12x18 Print", "12x18 in", "Unframed", 70, 160, 140, 5);

            long laddu = prod(cd, prasad, "Laddu Box", Const.TRACK_VARIANT);
            long lS = var(db, laddu, "Small Box", "250 g", "", 90, 140, 130, 10);
            long lL = var(db, laddu, "Large Box", "500 g", "", 170, 260, 240, 10);

            long album = prod(cd, albums, "Photo Album", Const.TRACK_VARIANT);
            long aW = var(db, album, "Wedding Album - 20 pages", "12x15 in", "Hardcover", 900, 1600, 1400, 2);
            var(db, album, "Wedding Album - 40 pages", "12x15 in", "Hardcover", 1500, 2600, 2300, 2);

            long key = prod(cd, acc, "Photo Keychain", Const.TRACK_VARIANT);
            long kC = var(db, key, "Round Keychain", "Standard", "", 15, 40, 30, 10);

            long pass = prod(cd, printing, "Passport Photos", Const.TRACK_VARIANT);
            long pP = var(db, pass, "Passport - 8 copies", "35x45 mm", "", 10, 60, 50, 20);

            // Opening stock (recorded as OPENING movements)
            long t0 = now - 3L * 24 * 3600 * 1000;
            stock(db, fSN, null, 25, ownerId, t0);
            stock(db, fSP, null, 18, ownerId, t0);
            stock(db, fSG, null, 4, ownerId, t0);   // intentionally low for the low-stock demo
            stock(db, fMN, null, 15, ownerId, t0);
            stock(db, fMP, null, 12, ownerId, t0);
            stock(db, fLW, null, 6, ownerId, t0);
            for (long v : new long[]{d8, d12}) {
                stock(db, v, dV, 20, ownerId, t0);
                stock(db, v, dG, 20, ownerId, t0);
                stock(db, v, dL, 15, ownerId, t0);
                stock(db, v, dM, 10, ownerId, t0);
            }
            stock(db, lS, null, 40, ownerId, t0);
            stock(db, lL, null, 30, ownerId, t0);
            stock(db, aW, null, 5, ownerId, t0);
            stock(db, kC, null, 60, ownerId, t0);
            stock(db, pP, null, 200, ownerId, t0);

            // A few sample sales (yesterday and today) so dashboards/reports have data
            long yesterday = now - 26L * 3600 * 1000;
            SaleProcessor.process(db, ramesh, lines(db, line(fSN, null, 150, 2), line(lS, null, 140, 3)),
                    Const.PAY_CASH, yesterday);
            SaleProcessor.process(db, ramesh, lines(db, line(fMP, null, 420, 1), line(d8, dV, 70, 2)),
                    Const.PAY_UPI, yesterday + 3600 * 1000);
            SaleProcessor.process(db, ramesh, lines(db, line(fLW, null, 800, 1), line(kC, null, 40, 4)),
                    Const.PAY_CASH, now - 600 * 1000);

            expense(db, ownerId, "Electricity", "Monthly electricity bill", 1850, now - 24L * 3600 * 1000);
            expense(db, ownerId, "Transport", "Frame delivery charges", 400, now);
            return null;
        });
    }

    private static long addUser(AppDatabase db, String username, String name, String pw, String role, long now) {
        User u = new User();
        u.username = username;
        u.fullName = name;
        u.passwordHash = PasswordUtil.hash(pw);
        u.role = role;
        u.active = true;
        u.createdAt = now;
        return db.userDao().insert(u);
    }

    private static long cat(CatalogDao cd, String name) {
        Category c = new Category();
        c.name = name;
        return cd.insertCategory(c);
    }

    private static long prod(CatalogDao cd, long categoryId, String name, String mode) {
        Product p = new Product();
        p.categoryId = categoryId;
        p.name = name;
        p.trackingMode = mode;
        return cd.insertProduct(p);
    }

    private static long design(CatalogDao cd, long productId, String name) {
        PhotoDesign d = new PhotoDesign();
        d.productId = productId;
        d.name = name;
        return cd.insertDesign(d);
    }

    private static long var(AppDatabase db, long productId, String name, String size, String frame,
                            double cost, double price, double min, int low) {
        Variant v = new Variant();
        v.productId = productId;
        v.name = name;
        v.size = size;
        v.frameType = frame;
        v.costPrice = cost;
        v.sellingPrice = price;
        v.minSellingPrice = min;
        long id = db.catalogDao().insertVariant(v);
        VariantStock vs = new VariantStock();
        vs.variantId = id;
        vs.lowStockThreshold = low;
        db.stockDao().insertVariantStock(vs);
        return id;
    }

    private static void stock(AppDatabase db, long variantId, Long designId, int qty, long userId, long ts) {
        StockOps.apply(db, variantId, designId, qty, Const.MOVE_OPENING, 0, "Opening stock", userId, ts);
    }

    private static CartLine line(long variantId, Long designId, double price, int qty) {
        CartLine l = new CartLine();
        l.variantId = variantId;
        l.designId = designId;
        l.unitPrice = price;
        l.quantity = qty;
        return l;
    }

    private static List<CartLine> lines(AppDatabase db, CartLine... ls) {
        List<CartLine> list = new ArrayList<>();
        for (CartLine l : ls) {
            list.add(l);
        }
        return list;
    }

    private static void expense(AppDatabase db, long userId, String cat, String desc, double amount, long ts) {
        Expense e = new Expense();
        e.category = cat;
        e.description = desc;
        e.amount = amount;
        e.date = DateUtil.startOfDay(ts);
        e.createdBy = userId;
        db.expenseDao().insert(e);
    }
}
