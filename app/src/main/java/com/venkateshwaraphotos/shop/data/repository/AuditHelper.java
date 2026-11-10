package com.venkateshwaraphotos.shop.data.repository;

import com.venkateshwaraphotos.shop.data.AppDatabase;
import com.venkateshwaraphotos.shop.data.entity.AuditLog;

final class AuditHelper {
    private AuditHelper() {}

    static void log(AppDatabase db, long userId, String action, String details) {
        AuditLog a = new AuditLog();
        a.userId = userId;
        a.action = action;
        a.details = details;
        a.timestamp = System.currentTimeMillis();
        db.auditDao().insert(a);
    }
}
