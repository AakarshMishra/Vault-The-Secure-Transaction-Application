package com.example.vault;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import com.scottyab.aescrypt.AESCrypt;

import java.security.GeneralSecurityException;

public class AddMoneyActivity extends AppCompatActivity {

    String username;
    TextView tvDisplayUsername, tv_upi;
    EditText etCredits;
    Button btnAddCredits;
    DatabaseReference creditsRef, transactionsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_money);

        // Initialize Firebase database references
        creditsRef = FirebaseDatabase.getInstance().getReference().child("Credits");
        transactionsRef = FirebaseDatabase.getInstance().getReference().child("Transactions");

        // Initialize views
        tvDisplayUsername = findViewById(R.id.tv_display_username);
        tv_upi = findViewById(R.id.tv_upi);
        etCredits = findViewById(R.id.et_credits);
        btnAddCredits = findViewById(R.id.btn_add_credits);

        // Get username from intent
        Intent i = getIntent();
        username = i.getStringExtra("username");
        tvDisplayUsername.setText("Username : "+username);
        tv_upi.setText("UPI ID : "+username+"@vault");

        btnAddCredits.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCreditsToAccount();
            }
        });
    }

    private void addCreditsToAccount() {
        String creditsStr = etCredits.getText().toString();
        if (creditsStr.isEmpty()) {
            Toast.makeText(this, "Please enter credits", Toast.LENGTH_SHORT).show();
            return;
        }

        int credits = Integer.parseInt(creditsStr);

        // Update credits in Firebase database
        creditsRef.child(username).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                int currentCredits = task.getResult().getValue(Integer.class);
                creditsRef.child(username).setValue(currentCredits + credits)
                        .addOnSuccessListener(aVoid -> {
                            // Store transaction details
                            String currentTime = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                            Transaction transaction = null;
                            try {
                                transaction = new Transaction(AESCrypt.encrypt("Vault",username), AESCrypt.encrypt("Vault","BANK"),credits, "ADDED FROM BANK");
                            } catch (GeneralSecurityException e) {
                                throw new RuntimeException(e);
                            }
                            transactionsRef.child(username).child(currentTime).setValue(transaction);

                            Toast.makeText(AddMoneyActivity.this, "Credits added successfully", Toast.LENGTH_SHORT).show();
                            etCredits.setText("");
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(AddMoneyActivity.this, "Failed to add credits", Toast.LENGTH_SHORT).show();
                        });
            } else {
                // If user doesn't exist, create a new entry
                creditsRef.child(username).setValue(credits)
                        .addOnSuccessListener(aVoid -> {
                            // Store transaction details
                            String currentTime = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                            Transaction transaction = null;
                            try {
                                transaction = new Transaction(AESCrypt.encrypt("Vault",username), AESCrypt.encrypt("Vault","BANK"), credits, "ADDED FROM BANK");
                            } catch (GeneralSecurityException e) {
                                throw new RuntimeException(e);
                            }
                            transactionsRef.child(username).child(currentTime).setValue(transaction);

                            Toast.makeText(AddMoneyActivity.this, "Credits added successfully", Toast.LENGTH_SHORT).show();
                            etCredits.setText("");
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(AddMoneyActivity.this, "Failed to add credits", Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }
}
