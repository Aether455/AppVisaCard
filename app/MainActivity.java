package com.example.appvisacard;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText usernameInput, passwordInput;
    private Button loginBtn, goToRegisterBtn;
    private UserDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupListeners();
    }

    private void initViews() {
        dbHelper = new UserDatabaseHelper(this);
        usernameInput = findViewById(R.id.usernameEditText);
        passwordInput = findViewById(R.id.passwordEditText);
        loginBtn = findViewById(R.id.loginButton);
        goToRegisterBtn = findViewById(R.id.registerButton);
    }

    private void setupListeners() {
        loginBtn.setOnClickListener(view -> attemptLogin());
        goToRegisterBtn.setOnClickListener(view ->
                startActivity(new Intent(this, RegisterActivity.class))
        );
    }

    private void attemptLogin() {
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            showToast("Vui lòng nhập tên đăng nhập");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            showToast("Vui lòng nhập mật khẩu");
            return;
        }

        if (dbHelper.checkLogin(username, password)) {
            showToast("Đăng nhập thành công!");
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        } else {
            showToast("Sai tên đăng nhập hoặc mật khẩu!");
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
