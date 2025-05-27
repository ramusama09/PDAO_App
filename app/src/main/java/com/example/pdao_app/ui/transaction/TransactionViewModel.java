package com.example.pdao_app.ui.transaction;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class TransactionViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public TransactionViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This list will show your most recent transactions");
    }

    public LiveData<String> getText() {
        return mText;
    }
}