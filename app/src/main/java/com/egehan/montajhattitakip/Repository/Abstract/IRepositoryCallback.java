package com.egehan.montajhattitakip.Repository.Abstract;

public interface IRepositoryCallback<T> {
    void onStart();
    void onComplete(T result);
    void onError(Exception e);
}