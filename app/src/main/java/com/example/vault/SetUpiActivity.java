package com.example.vault;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.scottyab.aescrypt.AESCrypt;

import java.security.GeneralSecurityException;

public class SetUpiActivity extends AppCompatActivity {

    private static final int IMAGE_REQUEST_CODE = 1;

    private ImageView imageViewStudent;
    private Uri imageUri;
    private ProgressDialog progressDialog;

    EditText editTextStudentTeacherID;
    String username;
    String studentID, studentName, studentClass, studentPhone, studentPassword, studentSchool, studentTeacherID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_upi);

        EditText editTextStudentID = findViewById(R.id.editTextStudentID);
        EditText editTextStudentName = findViewById(R.id.editTextStudentName);
        Intent i=getIntent();
        username=i.getStringExtra("username");


        Button buttonSignup = findViewById(R.id.buttonSignup);


        buttonSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the teacher details from the EditText fields
                studentID = editTextStudentID.getText().toString().trim();
                studentName = editTextStudentName.getText().toString().trim();



                // Validate the inputs
                if (studentID.isEmpty() || studentName.isEmpty() ) {
                    Toast.makeText(SetUpiActivity.this, "Please fill in all the details", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if(!studentID.equals(studentName))
                {
                    Toast.makeText(SetUpiActivity.this, "Please write same upi pin in both fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Show a progress dialog while uploading data and image
                progressDialog = new ProgressDialog(SetUpiActivity.this);
                progressDialog.setMessage("Setting up UPI...");
                progressDialog.setCancelable(false);
                progressDialog.show();

                // Validate teacherID before signing up the student


                // Upload the teacher image to Firebase Storage and get the download URL
                try {
                    saveUPIDetailsToDatabase(studentID);
                } catch (GeneralSecurityException e) {
                    throw new RuntimeException(e);
                }
            }
        });


    }







    private void saveUPIDetailsToDatabase(String studentID) throws GeneralSecurityException {
        // Get the reference to Firebase Realtime Database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("upi_data").child(username);

        // Create a Teacher object to save to the database
        Student1 student = new Student1(AESCrypt.encrypt("Vault",studentID));

        // Save the teacher object to the database
        databaseReference.setValue(student).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()) {
                    // Teacher signup success
                    Toast.makeText(SetUpiActivity.this, "UPI Setup successful", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    // Failed to save teacher details to the database
                    Toast.makeText(SetUpiActivity.this, "UPI Setup Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}

class Student1 {
    private String upinumber;



    // Empty constructor for Firebase
    public Student1() {
    }

    public Student1(String upinumber) {
        this.upinumber = upinumber;

    }

    public String getUpinumber() {
        return upinumber;
    }




}