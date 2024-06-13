package com.example.vault;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.scottyab.aescrypt.AESCrypt;

import java.security.GeneralSecurityException;

public class ShowPhotoActivity extends AppCompatActivity {

    private EditText editTextStudentID;
    private Button buttonSignup;
    private String username;
    private ImageView photoImageView;

    private DatabaseReference databaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_photo);

        editTextStudentID = findViewById(R.id.editTextStudentID);
        buttonSignup = findViewById(R.id.buttonSignup);
        photoImageView = findViewById(R.id.photo);
        Intent i=getIntent();
        username=i.getStringExtra("username");
        databaseReference = FirebaseDatabase.getInstance().getReference();



        buttonSignup.setOnClickListener(view -> {
            fetchImageFromFirebase(username);
        });
    }

    private void fetchImageFromFirebase(String studentID) {
        // Retrieve image URL from Firebase Realtime Database based on the entered student ID
        databaseReference.child("kyc_data").child(studentID).child("imageUrl")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String imageUrl = null;

                            imageUrl = dataSnapshot.getValue(String.class);

                        if (imageUrl != null) {
                            // Load image into ImageView using Picasso or Glide
                            Picasso.get().load(imageUrl).into(photoImageView);
                            // Make the ImageView visible
                            photoImageView.setVisibility(View.VISIBLE);
                        } else {
                            // If image URL is null, hide the ImageView
                            photoImageView.setVisibility(View.GONE);
                            // You can display a message to the user indicating that no image is found
                            Toast.makeText(ShowPhotoActivity.this, "No image found for this ID", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Handle errors here
                        Toast.makeText(ShowPhotoActivity.this, "Failed to fetch image: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
