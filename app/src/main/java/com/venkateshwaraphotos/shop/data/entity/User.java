package com.venkateshwaraphotos.shop.data.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "users", indices = {@Index(value = {"username"}, unique = true)})
public class User {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String username;
    public String fullName;
    public String passwordHash;
    public String role;
    public boolean active = true;
    public long createdAt;
}
