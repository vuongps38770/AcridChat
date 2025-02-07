package com.example.acrid.State;

import androidx.annotation.Nullable;

 public interface SimpleCallBack<T> {
    void onSucess(@Nullable T data);
    void onError(String message);
}
