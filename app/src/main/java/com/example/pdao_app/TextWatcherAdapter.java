package com.example.pdao_app;

import android.text.Editable;
import android.text.TextWatcher;

public class TextWatcherAdapter implements TextWatcher {
    private Runnable afterTextChangedRunnable;

    public TextWatcherAdapter(Runnable afterTextChangedRunnable) {
        this.afterTextChangedRunnable = afterTextChangedRunnable;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

    @Override
    public void afterTextChanged(Editable editable) {
        afterTextChangedRunnable.run();
    }
}
