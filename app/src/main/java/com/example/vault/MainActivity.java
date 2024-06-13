package com.example.vault;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import androidx.biometric.BiometricPrompt;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.crashlytics.buildtools.reloc.javax.annotation.Nonnull;

import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {

    Button btnAuth;
    TextView tvAuthStatus;

    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnAuth=findViewById(R.id.login);
        tvAuthStatus=findViewById(R.id.textView2);

        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt=new BiometricPrompt(MainActivity.this, executor,new BiometricPrompt.AuthenticationCallback(){
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString)
            {
                super.onAuthenticationError(errorCode,errString);
                tvAuthStatus.setText("Please Authenticate!");
            }

            @Override
            public void onAuthenticationSucceeded(@Nonnull BiometricPrompt.AuthenticationResult result){
                super.onAuthenticationSucceeded(result);
                tvAuthStatus.setText("Successfully Authenticated");
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }

            @Override
            public void onAuthenticationFailed(){
                super.onAuthenticationFailed();
                tvAuthStatus.setText("Authentication Failed");
            }
        });

        promptInfo=new BiometricPrompt.PromptInfo.Builder().setTitle("Biometric Authentication").setSubtitle("Login using Fingerprint or face").setNegativeButtonText("Cancel").build();

        btnAuth.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                biometricPrompt.authenticate(promptInfo);
            }
        });
    }
}