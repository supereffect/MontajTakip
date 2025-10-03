package com.egehan.montajhattitakip.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.bumptech.glide.Glide;
import com.egehan.montajhattitakip.MyApp;
import com.egehan.montajhattitakip.R;

public class LoginActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnLogin;
    TextView tvErrorMessage;

    SharedPreferences sharedPreferences;

    // Tek kullanıcı
    private static final String DEFAULT_EMAIL = "admin@montaj.com";
    private static final String DEFAULT_PASSWORD = "123456";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ImageView bgImage = findViewById(R.id.bgImage);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        try {
            Glide.with(this)
                    .load(MyApp.getWallpaperUrl())
                    .into(bgImage);
        } catch (Exception e) {
            var asdas = e;
        }
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvErrorMessage = findViewById(R.id.tvErrorMessage);

        sharedPreferences = getSharedPreferences("loginPrefs", Context.MODE_PRIVATE);

        // Daha önce login olmuşsa direkt MainActivity aç
        String savedEmail = sharedPreferences.getString("email", null);

        etEmail.setText(savedEmail);
        btnLogin.setOnClickListener(v -> {

            String email = String.valueOf(etEmail.getText());
            String password = DEFAULT_PASSWORD;

            if (!validateInput(email, password)) return;

            if (password.equals(DEFAULT_PASSWORD))
//            if (email.equals(DEFAULT_EMAIL) && password.equals(DEFAULT_PASSWORD))
            {
                saveCredentials(email);
                goToMain();
            } else {
                showError("Kullanıcı adı veya şifre yanlış!");
            }
        });
    }

    private boolean validateInput(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email boş olamaz");
            etEmail.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Şifre boş olamaz");
            etPassword.requestFocus();
            return false;
        }
        return true;
    }

    private void saveCredentials(String email) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("email", email);
        editor.apply();
    }

    private void showError(String message) {
        tvErrorMessage.setVisibility(View.VISIBLE);
        tvErrorMessage.setText(message);
    }

    private void goToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

