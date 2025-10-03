package com.egehan.montajhattitakip.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.egehan.montajhattitakip.R;
import com.egehan.montajhattitakip.Repository.Concrete.AuthRepository;
import com.egehan.montajhattitakip.Repository.Abstract.IRepositoryCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnLogin, btnRegister, btnForgotPassword;
    TextView tvErrorMessage;

    @Inject
    AuthRepository authRepository;

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        btnForgotPassword = findViewById(R.id.btnForgotPassword);
        tvErrorMessage = findViewById(R.id.tvErrorMessage);

        sharedPreferences = getSharedPreferences("loginPrefs", Context.MODE_PRIVATE);

        // Daha önce girilmiş bilgiler varsa otomatik doldur
        etEmail.setText(sharedPreferences.getString("email", ""));
        etPassword.setText(sharedPreferences.getString("password", ""));

        // LOGIN
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (!validateInput(email, password)) return;

            authRepository.login(email, password, new IRepositoryCallback<FirebaseUser>() {
                @Override
                public void onStart() {}

                @Override
                public void onComplete(FirebaseUser user) {
                    if (user != null) {
                        if (user.isEmailVerified()) {
                            saveCredentials(email, password);
                            goToMain();
                        } else {
                            FirebaseAuth.getInstance().signOut();
                            showError("Email adresiniz doğrulanmamış. Lütfen mailinizi kontrol edin.");
                        }
                    }
                }

                @Override
                public void onError(Exception e) {
                    showError("Giriş başarısız: " + e.getMessage());
                }
            });
        });

        // REGISTER
        btnRegister.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (!validateInput(email, password)) return;

            authRepository.register(email, password, new IRepositoryCallback<FirebaseUser>() {
                @Override
                public void onStart() {}

                @Override
                public void onComplete(FirebaseUser user) {
                    if (user != null) {
                        user.sendEmailVerification()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        showError("Kayıt başarılı! Lütfen e-posta adresinizi doğrulayın.");
                                    } else {
                                        showError("Kayıt başarılı ama doğrulama maili gönderilemedi.");
                                    }
                                });
                        saveCredentials(email, password);
                    }
                }

                @Override
                public void onError(Exception e) {
                    showError("Kayıt başarısız: " + e.getMessage());
                }
            });
        });

        // RESET PASSWORD
        btnForgotPassword.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (TextUtils.isEmpty(email)) {
                etEmail.setError("Lütfen email adresinizi girin");
                etEmail.requestFocus();
                return;
            }

            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            showError("Şifre sıfırlama linki mail adresinize gönderildi.");
                        } else {
                            showError("Şifre sıfırlama başarısız. Email adresinizi kontrol edin.");
                        }
                    });
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

        if (password.length() < 6) {
            etPassword.setError("Şifre en az 6 karakter olmalı");
            etPassword.requestFocus();
            return false;
        }
        return true;
    }

    private void saveCredentials(String email, String password) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("email", email);
        editor.putString("password", password);
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
