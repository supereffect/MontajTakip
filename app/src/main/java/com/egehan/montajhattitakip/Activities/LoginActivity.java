package com.egehan.montajhattitakip.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.egehan.montajhattitakip.R;
import com.egehan.montajhattitakip.Repository.Concrete.AuthRepository;
import com.egehan.montajhattitakip.Repository.Abstract.IRepositoryCallback;
import com.google.firebase.auth.FirebaseUser;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnLogin, btnRegister;

    @Inject
    AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            authRepository.login(email, password, new IRepositoryCallback<FirebaseUser>() {
                @Override
                public void onStart() {}

                @Override
                public void onComplete(FirebaseUser user) {
                    Toast.makeText(LoginActivity.this, "Giriş başarılı", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(LoginActivity.this, "Hata: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnRegister.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            authRepository.register(email, password, new IRepositoryCallback<FirebaseUser>() {
                @Override
                public void onStart() {}

                @Override
                public void onComplete(FirebaseUser user) {
                    Toast.makeText(LoginActivity.this, "Kayıt başarılı", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(LoginActivity.this, "Hata: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
