package com.example.vault;

import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import com.scottyab.aescrypt.AESCrypt;

import java.security.GeneralSecurityException;

public class AddBankAccountActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private BankAccountAdapter adapter;
    private List<BankAccount> bankAccountList;
    private DatabaseReference databaseRef;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_bank_account);

        Intent intent = getIntent();
        username = intent.getStringExtra("username");

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        bankAccountList = new ArrayList<>();
        adapter = new BankAccountAdapter(bankAccountList);
        recyclerView.setAdapter(adapter);

        // Add this code inside onCreate method after recyclerView setup

        FloatingActionButton fabAddBankAccount = findViewById(R.id.fabAddBankAccount);
        fabAddBankAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newBankAddIntent = new Intent(AddBankAccountActivity.this, NewBankAddActivity.class);
                newBankAddIntent.putExtra("username", username);
                startActivity(newBankAddIntent);
            }
        });

        // Connect to Firebase and fetch bank account data
        databaseRef = FirebaseDatabase.getInstance().getReference("BankAccounts").child(username);
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                bankAccountList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    BankAccount bankAccount = snapshot.getValue(BankAccount.class);
                    if (bankAccount != null) {
                        bankAccountList.add(bankAccount);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
            }
        });
    }

    class BankAccountAdapter extends RecyclerView.Adapter<BankAccountAdapter.ViewHolder> {
        private List<BankAccount> bankAccountList;

        public BankAccountAdapter(List<BankAccount> bankAccountList) {
            this.bankAccountList = bankAccountList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bank_account_card, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            BankAccount bankAccount = bankAccountList.get(position);
            try {
                holder.tvAccountNumber.setText("BANK ACCOUNT NUMBER: "+AESCrypt.decrypt("Vault",bankAccount.getAccountNumber()));
            } catch (GeneralSecurityException e) {
                throw new RuntimeException(e);
            }
            try {
                holder.tvIfsc.setText("IFSC CODE: "+AESCrypt.decrypt("Vault",bankAccount.getIfsc()));
            } catch (GeneralSecurityException e) {
                throw new RuntimeException(e);
            }
            try {
                holder.tvBankName.setText("BANK NAME: "+AESCrypt.decrypt("Vault",bankAccount.getBankName()));
            } catch (GeneralSecurityException e) {
                throw new RuntimeException(e);
            }
            try {
                holder.tvBranchCode.setText("BRANCH CODE: "+AESCrypt.decrypt("Vault",bankAccount.getBranchCode()));
            } catch (GeneralSecurityException e) {
                throw new RuntimeException(e);
            }
            try {
                holder.tvAccountType.setText("ACCOUNT TYPE: "+AESCrypt.decrypt("Vault",bankAccount.getAccountType()));
            } catch (GeneralSecurityException e) {
                throw new RuntimeException(e);
            }
            try {
                holder.tvAccountHolderName.setText("BANK HOLDER NAME: "+AESCrypt.decrypt("Vault",bankAccount.getAccountHolderName()));
            } catch (GeneralSecurityException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public int getItemCount() {
            return bankAccountList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvAccountNumber;
            TextView tvIfsc;
            TextView tvBankName;
            TextView tvBranchCode;
            TextView tvAccountType;
            TextView tvAccountHolderName;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvAccountNumber = itemView.findViewById(R.id.tvAccountNumber);
                tvIfsc = itemView.findViewById(R.id.tvIfsc);
                tvBankName = itemView.findViewById(R.id.tvBankName);
                tvBranchCode = itemView.findViewById(R.id.tvBranchCode);
                tvAccountType = itemView.findViewById(R.id.tvAccountType);
                tvAccountHolderName = itemView.findViewById(R.id.tvAccountHolderName);
            }
        }
    }
}

class BankAccount {
    private String accountNumber;
    private String ifsc;
    private String bankName;
    private String branchCode;
    private String accountType;
    private String accountHolderName;

    public BankAccount() {
        // Default constructor required for Firebase's getValue method
    }

    public BankAccount(String accountNumber, String ifsc, String bankName,
                       String branchCode, String accountType, String accountHolderName) {
        this.accountNumber = accountNumber;
        this.ifsc = ifsc;
        this.bankName = bankName;
        this.branchCode = branchCode;
        this.accountType = accountType;
        this.accountHolderName = accountHolderName;
    }

    // Getters and setters for all attributes

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getIfsc() {
        return ifsc;
    }

    public void setIfsc(String ifsc) {
        this.ifsc = ifsc;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getAccountHolderName() {
        return accountHolderName;
    }

    public void setAccountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName;
    }

    // Override toString() method for debugging or logging purposes

    @Override
    public String toString() {
        return "BankAccount{" +
                "accountNumber='" + accountNumber + '\'' +
                ", ifsc='" + ifsc + '\'' +
                ", bankName='" + bankName + '\'' +
                ", branchCode='" + branchCode + '\'' +
                ", accountType='" + accountType + '\'' +
                ", accountHolderName='" + accountHolderName + '\'' +
                '}';
    }
}
