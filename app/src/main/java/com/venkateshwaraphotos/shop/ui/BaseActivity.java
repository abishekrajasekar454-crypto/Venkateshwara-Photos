package com.venkateshwaraphotos.shop.ui;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.venkateshwaraphotos.shop.R;
import com.venkateshwaraphotos.shop.ShopApplication;

public abstract class BaseActivity extends AppCompatActivity {

    protected ShopApplication app() {
        return (ShopApplication) getApplication();
    }

    protected boolean isOwner() {
        return app().session.isOwner();
    }

    /**
     * Access guard. Call right after super.onCreate() and return if it yields false.
     * Sends logged-out users to Login and blocks employees from owner-only screens.
     */
    protected boolean guard(boolean ownerOnly) {
        if (!app().session.isLoggedIn()) {
            Intent i = new Intent(this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
            return false;
        }
        if (ownerOnly && !app().session.isOwner()) {
            Ui.toast(this, "This screen is for the owner only.");
            finish();
            return false;
        }
        return true;
    }

    protected MaterialToolbar setupToolbar(String title) {
        MaterialToolbar tb = findViewById(R.id.toolbar);
        tb.setTitle(title);
        tb.setNavigationOnClickListener(v -> finish());
        return tb;
    }

    protected void toast(String message) {
        Ui.toast(this, message);
    }
}
