package com.example.vault;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.scottyab.aescrypt.AESCrypt;

import java.security.GeneralSecurityException;

public class NewCardAddActivity extends AppCompatActivity {

    EditText cardNumberEditText, cvvEditText, expiryDateEditText, cardHolderNameEditText;
    Button addCardButton;
    String username;
    DatabaseReference cardsReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_card_add);

        Intent i = getIntent();
        username = i.getStringExtra("username");

        // Initialize Firebase Realtime Database reference
        cardsReference = FirebaseDatabase.getInstance().getReference().child("Cards").child(username);

        cardNumberEditText = findViewById(R.id.cardNumberEditText);
        cvvEditText = findViewById(R.id.cvvEditText);
        expiryDateEditText = findViewById(R.id.expiryDateEditText);
        cardHolderNameEditText = findViewById(R.id.cardHolderNameEditText);

        addCardButton = findViewById(R.id.addCardButton);
        addCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cardNumber = cardNumberEditText.getText().toString().trim();
                String cvv = cvvEditText.getText().toString().trim();
                String expiryDate = expiryDateEditText.getText().toString().trim();
                String cardHolderName = cardHolderNameEditText.getText().toString().trim();

                if (!cardNumber.isEmpty() && !cvv.isEmpty() && !expiryDate.isEmpty() && !cardHolderName.isEmpty()) {
                    try {
                        saveCardDetails(cardNumber, cvv, expiryDate, cardHolderName);
                    } catch (GeneralSecurityException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    // Handle empty fields
                }
            }
        });
    }

    private void saveCardDetails(String cardNumber, String cvv, String expiryDate, String cardHolderName) throws GeneralSecurityException {
        DatabaseReference specificCardReference = cardsReference.child(cardNumber);
        specificCardReference.child("cvv").setValue(AESCrypt.encrypt("Vault",cvv));
        specificCardReference.child("name").setValue(AESCrypt.encrypt("Vault",cardHolderName));
        specificCardReference.child("expiryDate").setValue(AESCrypt.encrypt("Vault",expiryDate));

        Toast.makeText(this, "Card Added Successfully!", Toast.LENGTH_SHORT).show();
        // Optionally add a success message or navigate back to previous activity
    }
}