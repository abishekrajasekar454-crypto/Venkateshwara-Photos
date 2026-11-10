package com.venkateshwaraphotos.shop.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.venkateshwaraphotos.shop.ShopApplication;
import com.venkateshwaraphotos.shop.data.entity.User;
import com.venkateshwaraphotos.shop.data.repository.UserRepository;
import com.venkateshwaraphotos.shop.util.Callback;

import java.util.List;

public class EmployeeViewModel extends AndroidViewModel {
    private final UserRepository repo;

    public EmployeeViewModel(@NonNull Application application) {
        super(application);
        repo = ((ShopApplication) application).userRepo;
    }

    public LiveData<List<User>> employees() {
        return repo.observeEmployees();
    }

    public void create(String username, String fullName, String password, Callback<Long> cb) {
        repo.createEmployee(username, fullName, password, cb);
    }

    public void update(User u, String fullName, boolean active, Callback<Void> cb) {
        repo.updateEmployee(u, fullName, active, cb);
    }

    public void resetPassword(long userId, String newPassword, Callback<Void> cb) {
        repo.resetPassword(userId, newPassword, cb);
    }
}
