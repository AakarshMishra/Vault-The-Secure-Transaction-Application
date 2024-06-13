package com.example.vault;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import android.os.Handler;
import android.os.Looper;

import android.os.Bundle;
import android.widget.Toast;


import com.example.vault.MainActivity;
import com.example.vault.R;

import java.util.concurrent.Executor;

public class splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Handler mHandler = new Handler(Looper.getMainLooper());

        mHandler.postDelayed(() -> {
            Intent intent = new Intent(splash.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }, 1500);

    }
}
