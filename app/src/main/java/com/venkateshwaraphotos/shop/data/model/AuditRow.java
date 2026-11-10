package com.venkateshwaraphotos.shop.data.model;

import androidx.room.Embedded;

import com.venkateshwaraphotos.shop.data.entity.AuditLog;

public class AuditRow {
    @Embedded
    public AuditLog log;
    public String userName;
}
