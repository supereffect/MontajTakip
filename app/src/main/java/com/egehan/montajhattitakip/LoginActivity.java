package com.egehan.montajhattitakip;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class LoginActivity extends AppCompatActivity {
    EditText etName;
    Button btnLogin;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        prefs = getSharedPreferences("AppData", MODE_PRIVATE);
        etName = findViewById(R.id.etName);
        btnLogin = findViewById(R.id.btnLogin);

        String savedName = prefs.getString("username", null);
//        if (savedName != null) {
//            startActivity(new Intent(this, MainActivity.class));
//            finish();
//        }
        etName.setText(savedName);

        btnLogin.setOnClickListener(v -> {
            String name = etName.getText().toString();
            if (!name.isEmpty()) {
                prefs.edit().putString("username", name).apply();
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        });
    }
}
