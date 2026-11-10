package com.venkateshwaraphotos.shop.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.venkateshwaraphotos.shop.ShopApplication;
import com.venkateshwaraphotos.shop.data.entity.User;
import com.venkateshwaraphotos.shop.util.Callback;

public class AuthViewModel extends AndroidViewModel {
    private final ShopApplication app;

    public AuthViewModel(@NonNull Application application) {
        super(application);
        app = (ShopApplication) application;
    }

    public void login(String username, String password, Callback<User> cb) {
        app.userRepo.login(username, password, cb);
    }

    public void logout() {
        app.userRepo.logout();
    }

    public void changePassword(String oldPw, String newPw, Callback<Void> cb) {
        app.userRepo.changeOwnPassword(oldPw, newPw, cb);
    }
}
