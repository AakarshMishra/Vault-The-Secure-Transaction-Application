package com.example.vault;

        import androidx.appcompat.app.AppCompatActivity;

        import android.os.Bundle;
        import android.view.View;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.Toast;

        import com.google.firebase.database.DatabaseReference;
        import com.google.firebase.database.FirebaseDatabase;
        import com.scottyab.aescrypt.AESCrypt;

        import java.security.GeneralSecurityException;

public class NewBankAddActivity extends AppCompatActivity {

    private EditText etAccountNumber, etIfsc, etBankName, etBranchCode, etAccountType, etAccountHolderName;
    private Button btnSaveBankAccount;
    private DatabaseReference databaseRef;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_bank_add);

        etAccountNumber = findViewById(R.id.etAccountNumber);
        etIfsc = findViewById(R.id.etIfsc);
        etBankName = findViewById(R.id.etBankName);
        etBranchCode = findViewById(R.id.etBranchCode);
        etAccountType = findViewById(R.id.etAccountType);
        etAccountHolderName = findViewById(R.id.etAccountHolderName);
        btnSaveBankAccount = findViewById(R.id.btnSaveBankAccount);

        username = getIntent().getStringExtra("username");
        databaseRef = FirebaseDatabase.getInstance().getReference().child("BankAccounts").child(username);

        btnSaveBankAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String accountNumber = etAccountNumber.getText().toString().trim();
                String ifsc = etIfsc.getText().toString().trim();
                String bankName = etBankName.getText().toString().trim();
                String branchCode = etBranchCode.getText().toString().trim();
                String accountType = etAccountType.getText().toString().trim();
                String accountHolderName = etAccountHolderName.getText().toString().trim();

                BankAccount bankAccount = null;
                try {
                    bankAccount = new BankAccount(AESCrypt.encrypt("Vault",accountNumber),
                            AESCrypt.encrypt("Vault",ifsc),AESCrypt.encrypt("Vault",bankName),
                            AESCrypt.encrypt("Vault",branchCode), AESCrypt.encrypt("Vault",accountType)
                            ,AESCrypt.encrypt("Vault",accountHolderName));
                } catch (GeneralSecurityException e) {
                    throw new RuntimeException(e);
                }
                databaseRef.child(accountNumber).setValue(bankAccount);

                // Optionally, you can add a success message to the user
                Toast.makeText(NewBankAddActivity.this, "Bank Account added successfully!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}