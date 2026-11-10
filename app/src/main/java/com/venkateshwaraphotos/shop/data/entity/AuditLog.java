package com.venkateshwaraphotos.shop.data.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "audit_logs", indices = {@Index("timestamp")})
public class AuditLog {
    @PrimaryKey(autoGenerate = true)
    public long id;
    /** 0 = system. */
    public long userId;
    public String action;
    public String details;
    public long timestamp;
}
