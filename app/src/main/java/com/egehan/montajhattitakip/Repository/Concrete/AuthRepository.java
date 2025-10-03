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
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null) {
                            user.sendEmailVerification()
                                    .addOnCompleteListener(verifyTask -> {
                                        if (verifyTask.isSuccessful()) {
                                            callback.onComplete(user);
                                        } else {
                                            callback.onError(verifyTask.getException());
                                        }
                                    });
                        } else {
                            callback.onError(new Exception("Kullanıcı oluşturulamadı"));
                        }
                    } else {
                        callback.onError(task.getException());
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
