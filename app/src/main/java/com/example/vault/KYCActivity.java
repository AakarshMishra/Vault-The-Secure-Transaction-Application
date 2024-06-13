package com.example.vault;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.scottyab.aescrypt.AESCrypt;

import java.io.ByteArrayOutputStream;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

public class KYCActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_kycactivity);
        FirebaseApp.initializeApp(this);
        Intent i = getIntent();
        username = i.getStringExtra("username");

        EditText editTextStudentID = findViewById(R.id.editTextStudentID);
        EditText editTextStudentName = findViewById(R.id.editTextStudentName);
        EditText editTextStudentClass = findViewById(R.id.editTextStudentClass);
        EditText editTextStudentPhone = findViewById(R.id.editTextStudentPhone);
        EditText editTextStudentSchool = findViewById(R.id.editTextStudentSchool);
        EditText editTextStudentPassword = findViewById(R.id.editTextStudentPassword);
        editTextStudentTeacherID = findViewById(R.id.editTextStudentTeacherID);

        Button buttonSignup = findViewById(R.id.buttonSignup);
        imageViewStudent = findViewById(R.id.imageViewStudent);

        buttonSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                studentID = editTextStudentID.getText().toString().trim();
                studentName = editTextStudentName.getText().toString().trim();
                studentClass = editTextStudentClass.getText().toString().trim();
                studentPhone = editTextStudentPhone.getText().toString().trim();
                studentSchool = editTextStudentSchool.getText().toString().trim();
                studentPassword = editTextStudentPassword.getText().toString().trim();
                studentTeacherID = editTextStudentTeacherID.getText().toString().trim();

                if (studentID.isEmpty() || studentName.isEmpty() || studentClass.isEmpty() ||
                        studentPhone.isEmpty() || studentSchool.isEmpty() || studentPassword.isEmpty() ||
                        studentTeacherID.isEmpty() || imageUri == null) {
                    Toast.makeText(KYCActivity.this, "Please fill in all the details", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressDialog = new ProgressDialog(KYCActivity.this);
                progressDialog.setMessage("Completing up KYC...");
                progressDialog.setCancelable(false);
                progressDialog.show();

                try {
                    String barcodeContent = username + "@vault";
                    Bitmap barcodeBitmap = generateBarcode(barcodeContent);
                    uploadBarcode(barcodeBitmap, studentID, studentName, studentClass, studentPhone, studentPassword, studentSchool, studentTeacherID);
                } catch (WriterException e) {
                    Toast.makeText(KYCActivity.this, "Failed to generate barcode", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }
        });

        imageViewStudent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage();
            }
        });
    }

    private Bitmap generateBarcode(String content) throws WriterException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, 512, 512, hints);
        Bitmap bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.RGB_565);
        for (int x = 0; x < 512; x++) {
            for (int y = 0; y < 512; y++) {
                bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }
        return bitmap;
    }

    private void pickImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imageViewStudent.setImageURI(imageUri);
        }
    }

    private void uploadBarcode(Bitmap barcodeBitmap, String studentID, String studentName, String studentClass, String studentPhone, String studentPassword, String studentSchool, String studentTeacherID) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        barcodeBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReference().child("userid_barcodes").child(username + "_barcode.jpg");

        UploadTask uploadTask = storageReference.putBytes(data);
        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return storageReference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri barcodeDownloadUri = task.getResult();
                    if (barcodeDownloadUri != null) {
                        String barcodeImageUrl = barcodeDownloadUri.toString();
                        uploadStudentImage(studentID, studentName, studentClass, studentPhone, studentPassword, studentSchool, studentTeacherID, barcodeImageUrl);
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(KYCActivity.this, "Failed to upload barcode", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(KYCActivity.this, "Failed to upload barcode", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void uploadStudentImage(String studentID, String studentName, String studentClass, String studentPhone, String studentPassword, String studentSchool, String studentTeacherID, String barcodeImageUrl) {
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReference().child("userid_images").child(username + ".jpg");

        UploadTask uploadTask = storageReference.putFile(imageUri);
        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return storageReference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    if (downloadUri != null) {
                        String imageUrl = downloadUri.toString();

                        try {
                            saveStudentDetailsToDatabase(studentID, studentName, studentClass, studentPhone, studentPassword, studentSchool, studentTeacherID, imageUrl, barcodeImageUrl);
                        } catch (GeneralSecurityException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(KYCActivity.this, "Failed to upload student image", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(KYCActivity.this, "Failed to upload student image", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void saveStudentDetailsToDatabase(String studentID, String studentName, String studentClass, String studentPhone, String studentPassword, String studentSchool, String studentTeacherID, String imageUrl, String barcodeImageUrl) throws GeneralSecurityException {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("kyc_data").child(username);

        Student student = new Student(
                AESCrypt.encrypt("Vault", studentID),
                AESCrypt.encrypt("Vault", studentName),
                AESCrypt.encrypt("Vault", studentClass),
                AESCrypt.encrypt("Vault", studentPhone),
                AESCrypt.encrypt("Vault", studentPassword),
                AESCrypt.encrypt("Vault", studentSchool),
                AESCrypt.encrypt("Vault", studentTeacherID),
                imageUrl,
                barcodeImageUrl
        );

        databaseReference.setValue(student).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    addCreditsToDatabase(username);  // Adding credits to the database
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(KYCActivity.this, "KYC Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void addCreditsToDatabase(String username) {
        DatabaseReference creditsReference = FirebaseDatabase.getInstance().getReference().child("Credits").child(username);
        creditsReference.setValue(0).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()) {
                    Toast.makeText(KYCActivity.this, "KYC successful", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(KYCActivity.this, "KYC Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    class Student {
        private String phonenumber;
        private String Name;
        private String pincode;
        private String City;
        private String address;
        private String photoidname;
        private String photoidnumber;
        private String imageUrl;
        private String barcodeImageUrl;

        public Student() {
        }

        public Student(String phonenumber, String Name, String pincode, String City, String address, String photoidname, String photoidnumber, String imageUrl, String barcodeImageUrl) {
            this.phonenumber = phonenumber;
            this.Name = Name;
            this.pincode = pincode;
            this.City = City;
            this.address = address;
            this.photoidname = photoidname;
            this.photoidnumber = photoidnumber;
            this.imageUrl = imageUrl;
            this.barcodeImageUrl = barcodeImageUrl;
        }

        public String getPhonenumber() {
            return phonenumber;
        }

        public String getName() {
            return Name;
        }

        public String getPincode() {
            return pincode;
        }

        public String getCity() {
            return City;
        }

        public String getAddress() {
            return address;
        }

        public String getPhotoidname() {
            return photoidname;
        }

        public String getPhotoidnumber() {
            return photoidnumber;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public String getBarcodeImageUrl() {
            return barcodeImageUrl;
        }
    }
}
