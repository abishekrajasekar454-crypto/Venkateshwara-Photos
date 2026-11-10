package com.venkateshwaraphotos.shop.data.repository;

import androidx.lifecycle.LiveData;

import com.venkateshwaraphotos.shop.data.AppDatabase;
import com.venkateshwaraphotos.shop.data.Const;
import com.venkateshwaraphotos.shop.data.entity.User;
import com.venkateshwaraphotos.shop.util.AppExecutors;
import com.venkateshwaraphotos.shop.util.Callback;
import com.venkateshwaraphotos.shop.util.PasswordUtil;
import com.venkateshwaraphotos.shop.util.SessionManager;
import com.venkateshwaraphotos.shop.util.ShopException;

import java.util.List;

/**
 * Local authentication + employee management.
 * Firebase note: replace login()/createEmployee() with FirebaseAuth calls later; the UI only talks to this class.
 */
public class UserRepository {
    private final AppDatabase db;
    private final AppExecutors ex;
    private final SessionManager session;

    public UserRepository(AppDatabase db, AppExecutors ex, SessionManager session) {
        this.db = db;
        this.ex = ex;
        this.session = session;
    }

    public LiveData<List<User>> observeEmployees() {
        return db.userDao().observeEmployees();
    }

    public LiveData<List<User>> observeAllUsers() {
        return db.userDao().observeAll();
    }

    public void login(final String username, final String password, Callback<User> cb) {
        ex.run(() -> {
            if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
                throw new ShopException("Enter username and password.");
            }
            User u = db.userDao().findByUsername(username.trim());
            if (u == null || !PasswordUtil.verify(password, u.passwordHash)) {
                throw new ShopException("Invalid username or password.");
            }
            if (!u.active) {
                throw new ShopException("This account is disabled. Please contact the owner.");
            }
            session.login(u);
            AuditHelper.log(db, u.id, "LOGIN", u.username + " logged in");
            return u;
        }, cb);
    }

    public void logout() {
        session.logout();
    }

    public void createEmployee(final String username, final String fullName, final String password, Callback<Long> cb) {
        ex.run(() -> {
            session.requireOwner();
            validateName(fullName);
            if (username == null || !username.trim().matches("[A-Za-z0-9._-]{3,30}")) {
                throw new ShopException("Username must be 3-30 letters, digits, dot, dash or underscore.");
            }
            validatePassword(password);
            User u = new User();
            u.username = username.trim();
            u.fullName = fullName.trim();
            u.passwordHash = PasswordUtil.hash(password);
            u.role = Const.EMPLOYEE;
            u.active = true;
            u.createdAt = System.currentTimeMillis();
            long id = db.userDao().insert(u);
            AuditHelper.log(db, session.getUserId(), "EMPLOYEE_CREATE", "Created employee " + u.username);
            return id;
        }, cb);
    }

    public void updateEmployee(final User user, final String fullName, final boolean active, Callback<Void> cb) {
        ex.run(() -> {
            session.requireOwner();
            validateName(fullName);
            User u = db.userDao().findById(user.id);
            if (u == null || !Const.EMPLOYEE.equals(u.role)) throw new ShopException("Employee not found.");
            u.fullName = fullName.trim();
            u.active = active;
            db.userDao().update(u);
            AuditHelper.log(db, session.getUserId(), "EMPLOYEE_UPDATE", "Updated " + u.username + (active ? "" : " (disabled)"));
            return null;
        }, cb);
    }

    public void resetPassword(final long userId, final String newPassword, Callback<Void> cb) {
        ex.run(() -> {
            session.requireOwner();
            validatePassword(newPassword);
            User u = db.userDao().findById(userId);
            if (u == null) throw new ShopException("User not found.");
            u.passwordHash = PasswordUtil.hash(newPassword);
            db.userDao().update(u);
            AuditHelper.log(db, session.getUserId(), "PASSWORD_RESET", "Reset password for " + u.username);
            return null;
        }, cb);
    }

    public void changeOwnPassword(final String oldPassword, final String newPassword, Callback<Void> cb) {
        ex.run(() -> {
            session.requireLogin();
            validatePassword(newPassword);
            User u = db.userDao().findById(session.getUserId());
            if (u == null || !PasswordUtil.verify(oldPassword, u.passwordHash)) {
                throw new ShopException("Current password is incorrect.");
            }
            u.passwordHash = PasswordUtil.hash(newPassword);
            db.userDao().update(u);
            AuditHelper.log(db, u.id, "PASSWORD_CHANGE", u.username + " changed own password");
            return null;
        }, cb);
    }

    private static void validateName(String name) {
        if (name == null || name.trim().isEmpty()) throw new ShopException("Full name is required.");
    }

    private static void validatePassword(String p) {
        if (p == null || p.length() < 6) throw new ShopException("Password must be at least 6 characters.");
    }
}
