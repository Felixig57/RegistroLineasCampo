package com.example.registrolineascampo.ui.Settings;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

public class MiApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }
}