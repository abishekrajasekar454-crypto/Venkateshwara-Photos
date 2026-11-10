package com.venkateshwaraphotos.shop.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.venkateshwaraphotos.shop.data.Const;
import com.venkateshwaraphotos.shop.data.entity.User;

public class SessionManager {
    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences("session", Context.MODE_PRIVATE);
    }

    public void login(User u) {
        prefs.edit().putLong("id", u.id).putString("name", u.fullName).putString("role", u.role).apply();
    }

    public void logout() {
        prefs.edit().clear().apply();
    }

    public boolean isLoggedIn() {
        return prefs.getLong("id", 0) > 0;
    }

    public long getUserId() {
        return prefs.getLong("id", 0);
    }

    public String getName() {
        return prefs.getString("name", "");
    }

    public boolean isOwner() {
        return Const.OWNER.equals(prefs.getString("role", ""));
    }

    public void requireOwner() {
        if (!isOwner()) {
            throw new ShopException("Only the owner can do this.");
        }
    }

    public void requireLogin() {
        if (!isLoggedIn()) {
            throw new ShopException("Please log in again.");
        }
    }
}
