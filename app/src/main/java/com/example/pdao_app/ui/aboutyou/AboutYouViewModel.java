package com.example.pdao_app.ui.aboutyou;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AboutYouViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public AboutYouViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is slideshow fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}