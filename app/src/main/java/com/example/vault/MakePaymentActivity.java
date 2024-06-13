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

public class MakePaymentActivity extends AppCompatActivity {

    String username, receiverUPI;
    TextView tvReceiverUPI, tvUsername;
    EditText etCredits;
    Button btnTransfer;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_payment);

        Intent i = getIntent();
        receiverUPI = i.getStringExtra("receiverUPI");
        receiverUPI = receiverUPI.substring(0, receiverUPI.indexOf('@'));
        username = i.getStringExtra("username");

        tvReceiverUPI = findViewById(R.id.tvReceiverUPI);
        tvUsername = findViewById(R.id.tvUsername);
        etCredits = findViewById(R.id.etCredits);
        btnTransfer = findViewById(R.id.btnTransfer);

        tvReceiverUPI.setText("Receiver UPI Id: " + receiverUPI + "@vault");
        tvUsername.setText("My UPI Id: " + username + "@vault");

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Credits");

        btnTransfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transferCredits();
            }
        });
    }

    private void transferCredits() {
        final String creditsToTransferStr = etCredits.getText().toString().trim();

        if (creditsToTransferStr.isEmpty()) {
            Toast.makeText(this, "Please enter credits to transfer", Toast.LENGTH_SHORT).show();
            return;
        }

        final int creditsToTransfer = Integer.parseInt(creditsToTransferStr);

        databaseReference.child(username).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                long myCredits = task.getResult().getValue(Long.class);

                if (myCredits < creditsToTransfer) {
                    Toast.makeText(this, "Insufficient credits", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    long updatedMyCredits = myCredits - creditsToTransfer;
                    final long[] updatedReceiverCredits = {0};

                    databaseReference.child(receiverUPI).get().addOnCompleteListener(receiverTask -> {
                        if (receiverTask.isSuccessful()) {
                            long receiverCredits = receiverTask.getResult().getValue(Long.class);
                            updatedReceiverCredits[0] = receiverCredits + creditsToTransfer;
                        }

                        // Update sender's credits
                        databaseReference.child(username).setValue(updatedMyCredits);
                        // Update receiver's credits
                        databaseReference.child(receiverUPI).setValue(updatedReceiverCredits[0]);

                        // Store transaction details
                        String currentTime = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                        DatabaseReference transactionsRef = FirebaseDatabase.getInstance().getReference().child("Transactions");

                        // Transaction from sender
                        Transaction senderTransaction = null;
                        try {
                            senderTransaction = new Transaction(AESCrypt.encrypt("Vault",username), AESCrypt.encrypt("Vault",receiverUPI), creditsToTransfer, "PAID TO");
                        } catch (GeneralSecurityException e) {
                            throw new RuntimeException(e);
                        }
                        transactionsRef.child(username).child(currentTime).setValue(senderTransaction);

                        // Transaction for receiver
                        Transaction receiverTransaction = null;
                        try {
                            receiverTransaction = new Transaction(AESCrypt.encrypt("Vault",username), AESCrypt.encrypt("Vault",receiverUPI), creditsToTransfer, "RECEIVED FROM");
                        } catch (GeneralSecurityException e) {
                            throw new RuntimeException(e);
                        }
                        transactionsRef.child(receiverUPI).child(currentTime).setValue(receiverTransaction);

                        Toast.makeText(this, "Credits transferred successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }
            } else {
                Toast.makeText(this, "Error getting credits", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
class Transaction {
    private String username;
    private String receiverUPI;
    private int credits;
    private String type;

    public Transaction() {
        // Default constructor required for calls to DataSnapshot.getValue(Transaction.class)
    }

    public Transaction(String username, String receiverUPI, int credits, String type) {
        this.username = username;
        this.receiverUPI = receiverUPI;
        this.credits = credits;
        this.type = type;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getReceiverUPI() {
        return receiverUPI;
    }

    public void setReceiverUPI(String receiverUPI) {
        this.receiverUPI = receiverUPI;
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
