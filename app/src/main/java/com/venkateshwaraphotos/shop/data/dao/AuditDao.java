package com.venkateshwaraphotos.shop.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.venkateshwaraphotos.shop.data.entity.AuditLog;
import com.venkateshwaraphotos.shop.data.model.AuditRow;

import java.util.List;

@Dao
public interface AuditDao {
    @Insert
    long insert(AuditLog log);

    @Query("SELECT a.*, COALESCE(u.fullName, 'System') AS userName FROM audit_logs a "
            + "LEFT JOIN users u ON u.id = a.userId ORDER BY a.timestamp DESC, a.id DESC LIMIT 100")
    List<AuditRow> recentSync();
}
