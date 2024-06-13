package com.example.vault;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.scottyab.aescrypt.AESCrypt;

import java.security.GeneralSecurityException;

public class SignupActivity extends AppCompatActivity {

    EditText signupName, signupEmail, signupUsername, signupPassword, signup_phone;
    TextView loginRedirectText;
    Button signupButton;
    FirebaseDatabase database;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        signupName = findViewById(R.id.signup_name);
        signupEmail = findViewById(R.id.signup_email);
        signupUsername = findViewById(R.id.signup_username);
        signupPassword = findViewById(R.id.signup_password);
        signupButton = findViewById(R.id.signup_button);
        loginRedirectText = findViewById(R.id.loginRedirectText);
        signup_phone = findViewById(R.id.signup_phone);

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                database = FirebaseDatabase.getInstance();
                reference = database.getReference("users");

                String name = null;
                try {
                    name = AESCrypt.encrypt("Vault",signupName.getText().toString());
                } catch (GeneralSecurityException e) {
                    throw new RuntimeException(e);
                }
                String email = null;
                try {
                    email = AESCrypt.encrypt("Vault", signupEmail.getText().toString());
                } catch (GeneralSecurityException e) {
                    throw new RuntimeException(e);
                }
                String username = signupUsername.getText().toString();

                String password = null;
                try {
                    password = AESCrypt.encrypt("Vault", signupPassword.getText().toString());
                } catch (GeneralSecurityException e) {
                    throw new RuntimeException(e);
                }

                HelperClass helperClass = new HelperClass(name, email, username, signup_phone.getText().toString().trim(), password);
                reference.child(username).setValue(helperClass);

                Toast.makeText(SignupActivity.this, "You have signup successfully!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        loginRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }
}