package com.venkateshwaraphotos.shop.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.venkateshwaraphotos.shop.R;
import com.venkateshwaraphotos.shop.ShopApplication;
import com.venkateshwaraphotos.shop.data.entity.User;
import com.venkateshwaraphotos.shop.util.Callback;
import com.venkateshwaraphotos.shop.viewmodel.AuthViewModel;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ShopApplication app = (ShopApplication) getApplication();
        if (app.session.isLoggedIn()) {
            route(app.session.isOwner());
            return;
        }
        setContentView(R.layout.activity_login);
        final AuthViewModel vm = new ViewModelProvider(this).get(AuthViewModel.class);
        final EditText etUser = findViewById(R.id.etUsername);
        final EditText etPass = findViewById(R.id.etPassword);
        final MaterialButton btn = findViewById(R.id.btnLogin);
        btn.setOnClickListener(v -> {
            btn.setEnabled(false);
            vm.login(etUser.getText().toString(), etPass.getText().toString(), new Callback<User>() {
                @Override
                public void onSuccess(User user) {
                    etPass.setText("");
                    route(app.session.isOwner());
                }

                @Override
                public void onError(String message) {
                    btn.setEnabled(true);
                    Ui.toast(LoginActivity.this, message);
                }
            });
        });
    }

    private void route(boolean owner) {
        startActivity(new Intent(this, owner ? OwnerDashboardActivity.class : EmployeeDashboardActivity.class));
        finish();
    }
}
