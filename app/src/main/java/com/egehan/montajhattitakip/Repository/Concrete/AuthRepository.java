package com.egehan.montajhattitakip.Repository.Concrete;

import androidx.annotation.NonNull;

import com.egehan.montajhattitakip.Repository.Abstract.IRepositoryCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AuthRepository {
    private final FirebaseAuth mAuth;

    @Inject
    public AuthRepository() {
        mAuth = FirebaseAuth.getInstance();
    }

    public void login(String email, String password, IRepositoryCallback<FirebaseUser> callback) {
        callback.onStart();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && mAuth.getCurrentUser() != null) {
                        callback.onComplete(mAuth.getCurrentUser());
                    } else {
                        callback.onError(task.getException() != null ? task.getException() : new Exception("Giriş başarısız"));
                    }
                });
    }

    public void register(String email, String password, IRepositoryCallback<FirebaseUser> callback) {
        callback.onStart();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && mAuth.getCurrentUser() != null) {
                        callback.onComplete(mAuth.getCurrentUser());
                    } else {
                        callback.onError(task.getException() != null ? task.getException() : new Exception("Kayıt başarısız"));
                    }
                });
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public void logout() {
        mAuth.signOut();
    }
}
