package com.egehan.montajhattitakip.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.TextWatcher;
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

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvErrorMessage;
    private SharedPreferences sharedPreferences;

    private static final String DEFAULT_EMAIL = "admin@montaj.com";
    private static final String DEFAULT_PASSWORD = "123456";
    private static final String PREF_NAME = "loginPrefs";
    public static String email;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        initViews();
        loadBackground();
        setupListeners();
        restoreSavedCredentials();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvErrorMessage = findViewById(R.id.tvErrorMessage);
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    private void loadBackground() {
        try {
            ImageView bgImage = findViewById(R.id.bgImage);
            Glide.with(this).load(MyApp.getWallpaperUrl()).into(bgImage);
        } catch (Exception ignored) { }
    }

    private void restoreSavedCredentials() {
        etEmail.setText(sharedPreferences.getString("email", ""));
        etPassword.setText(sharedPreferences.getString("password", ""));
    }

    private void setupListeners() {
        etEmail.addTextChangedListener(simpleTextWatcher(s -> {
            etPassword.setVisibility(s.equals("admin") ? View.VISIBLE : View.GONE);
        }));

        btnLogin.setOnClickListener(v -> handleLogin());
    }

    private void handleLogin() {
        String email = etEmail.getText().toString().trim();
        String inputPassword = etPassword.getText().toString().trim();
        String defaultPassword = DEFAULT_PASSWORD;

        if (!validateInput(email)) return;

        boolean isAdmin = email.equals("admin");
        boolean isAdminPasswordCorrect = inputPassword.equals(MyApp.getWallpaperParamter());
        boolean isDefaultLogin = !isAdmin && defaultPassword.equals(DEFAULT_PASSWORD);

        if (isAdmin && isAdminPasswordCorrect) {
            this.email = email;
            saveCredentials(email, inputPassword);
            goToMain();
        } else if (isDefaultLogin) {
            this.email = email;

            saveCredentials(email, defaultPassword);
            goToMain();
        } else {
            showError("Kullanıcı adı veya şifre yanlış!");
        }
    }

    private boolean validateInput(String email) {
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email boş olamaz");
            etEmail.requestFocus();
            return false;
        }

        if (etPassword.getVisibility() == View.VISIBLE && TextUtils.isEmpty(etPassword.getText())) {
            etPassword.setError("Şifre boş olamaz");
            etPassword.requestFocus();
            return false;
        }
        return true;
    }

    private void saveCredentials(String email, String password) {
        sharedPreferences.edit()
                .putString("email", email)
                .putString("password", password)
                .apply();
    }

    private void showError(String message) {
        tvErrorMessage.setVisibility(View.VISIBLE);
        tvErrorMessage.setText(message);
    }

    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }

    // Basit TextWatcher helper
    private TextWatcher simpleTextWatcher(OnTextChanged listener) {
        return new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                listener.onTextChanged(s.toString());
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        };
    }

    private interface OnTextChanged {
        void onTextChanged(String s);
    }

}
