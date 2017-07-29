package com.lany.uitransfer.annotaion;

public interface TransferInjector<T> {
    void inject(T a, Object i);
}
