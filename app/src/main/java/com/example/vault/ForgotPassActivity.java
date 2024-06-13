package com.example.vault;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.scottyab.aescrypt.AESCrypt;

import java.security.GeneralSecurityException;

import java.util.Random;

public class ForgotPassActivity extends AppCompatActivity {

    private EditText usernameEditText, otpEditText;
    private Button generateOTPButton, verifyOTPButton;

    private DatabaseReference usersRef, otpRef;

    private static final int SMS_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_pass);

        usernameEditText = findViewById(R.id.usernameEditText);
        otpEditText = findViewById(R.id.otpEditText);
        generateOTPButton = findViewById(R.id.generateOTPButton);
        verifyOTPButton = findViewById(R.id.verifyOTPButton);

        usersRef = FirebaseDatabase.getInstance().getReference().child("users");
        otpRef = FirebaseDatabase.getInstance().getReference().child("OTP");

        generateOTPButton.setOnClickListener(v -> {
            // Generate OTP and send it via SMS
            String username = usernameEditText.getText().toString().trim();
            usersRef.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String phoneNumber = dataSnapshot.child("phone").getValue(String.class);
                        if (phoneNumber != null) {
                            String randomOTP = generateRandomOTP();
                            saveOTPToFirebase(username, randomOTP);
                            // Inside generateOTPButton OnClickListener
                            if (checkSMSPermission()) {
                                sendSMS(phoneNumber, "Your OTP is: " + randomOTP);
                                Toast.makeText(ForgotPassActivity.this, "OTP sent via SMS", Toast.LENGTH_SHORT).show();

                                // Make otpEditText and verifyOTPButton visible after successful OTP send
                                otpEditText.setVisibility(View.VISIBLE);
                                verifyOTPButton.setVisibility(View.VISIBLE);
                            } else {
                                requestSMSPermission();
                            }
                        } else {
                            Toast.makeText(ForgotPassActivity.this, "Phone number not found for " + username, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ForgotPassActivity.this, "User not found with username " + username, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(ForgotPassActivity.this, "Error retrieving data", Toast.LENGTH_SHORT).show();
                }
            });
        });

        verifyOTPButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String enteredOTP = otpEditText.getText().toString().trim();

            otpRef.child(username).child("otp").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String savedOTP = dataSnapshot.getValue(String.class);
                    if (savedOTP != null && savedOTP.equals(enteredOTP)) {
                        // OTP matched, fetch user's password and display in a dialog
                        usersRef.child(username).child("password").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot passwordSnapshot) {
                                String password = passwordSnapshot.getValue(String.class);
                                if (password != null) {
                                    // Show password in dialog
                                    try {
                                        showPasswordDialog(password);
                                    } catch (GeneralSecurityException e) {
                                        throw new RuntimeException(e);
                                    }
                                } else {
                                    Toast.makeText(ForgotPassActivity.this, "Password not found for " + username, Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(ForgotPassActivity.this, "Error retrieving password", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(ForgotPassActivity.this, "Incorrect OTP", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ForgotPassActivity.this, "Error retrieving OTP", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private String generateRandomOTP() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // Generate a 6-digit OTP
        return String.valueOf(otp);
    }

    private void saveOTPToFirebase(String username, String otp) {
        otpRef.child(username).child("otp").setValue(otp, (error, ref) -> {
            if (error != null) {
                Toast.makeText(ForgotPassActivity.this, "Error saving OTP to database", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendSMS(String phoneNumber, String message) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, message, null, null);
    }

    private boolean checkSMSPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestSMSPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_REQUEST_CODE);
    }

    private void showPasswordDialog(String password) throws GeneralSecurityException {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Your Password");
        builder.setMessage("Your password is: " + AESCrypt.decrypt("Vault",password));
        builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}