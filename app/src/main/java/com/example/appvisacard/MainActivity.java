package com.example.appvisacard;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    EditText emailInput, passwordInput;
    Button loginBtn, goToRegisterBtn;
    UserDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new UserDatabaseHelper(this);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginBtn = findViewById(R.id.loginBtn);
        goToRegisterBtn = findViewById(R.id.goToRegisterBtn);

        loginBtn.setOnClickListener(view -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (dbHelper.checkLogin(email, password)) {
                Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, HomeActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Sai thông tin đăng nhập!", Toast.LENGTH_SHORT).show();
            }
        });

        goToRegisterBtn.setOnClickListener(view -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }
}
