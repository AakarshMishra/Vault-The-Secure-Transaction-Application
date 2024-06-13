package com.example.vault.fragment;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vault.R;
import com.example.vault.adapter.TransactionsAdapter;
import com.example.vault.model.TransactionModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import com.scottyab.aescrypt.AESCrypt;

import java.security.GeneralSecurityException;

/**
 * A simple {@link Fragment} subclass.
 */

public class TransactionsFragment extends Fragment {
    private Context context;
    private RecyclerView mRecyclerview;
    private DatabaseReference transactionsRef;
    static TransactionsFragment fragment;
    private String usernamee;

    public TransactionsFragment() {
    }

    public static TransactionsFragment newInstance(String usernamee) {
        fragment = new TransactionsFragment();
        Bundle args = new Bundle();
        args.putString("username", usernamee);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            usernamee = getArguments().getString("username");
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    public static TransactionsFragment newInstance() {
        TransactionsFragment fragment = new TransactionsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    private void initViews(View view) {
        mRecyclerview = view.findViewById(R.id.rv_transactions);
        mRecyclerview.setLayoutManager(new LinearLayoutManager(context));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transactions, container, false);
        initViews(view);

        // Firebase setup
        transactionsRef = FirebaseDatabase.getInstance().getReference().child("Transactions");

        // Retrieve transaction data from Firebase
        retrieveTransactions();

        return view;
    }

    private void retrieveTransactions() {
        transactionsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<TransactionModel> transactionList = new ArrayList<>();
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot timeSnapshot : userSnapshot.getChildren()) {
                        String username = userSnapshot.getKey();
//
                        String time = timeSnapshot.getKey();
                        String type = timeSnapshot.child("type").getValue(String.class);
                        String receiverUPI = timeSnapshot.child("receiverUPI").getValue(String.class);
                        int credits = timeSnapshot.child("credits").getValue(Integer.class);


                        String transactionMerchant = type.equals("paid") ? receiverUPI : receiverUPI;
                        String transactionCreditedDebited = type.equals("PAID TO") ? "Debited From" : "Credited To";

//                        // Format date
//                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
//                        String dateString = sdf.format(new Date(Long.parseLong(time)));


                        if(username.equals(usernamee)){

                            if(type.equals("PAID TO")) {
                                try {
                                    transactionList.add(new TransactionModel(R.drawable.ic_to_contact, "ID: "+time, type, AESCrypt.decrypt("Vault",transactionMerchant),
                                            context.getResources().getString(R.string.rupees) + credits, transactionCreditedDebited));
                                } catch (GeneralSecurityException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            else {
                                try {
                                    transactionList.add(new TransactionModel(R.drawable.ic_to_self, "ID: "+time, type, AESCrypt.decrypt("Vault",transactionMerchant),
                                            context.getResources().getString(R.string.rupees) + credits, transactionCreditedDebited));
                                } catch (GeneralSecurityException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                        // Add transaction to list

                    }
                }

                // Set up RecyclerView adapter
                TransactionsAdapter adapter = new TransactionsAdapter(context, transactionList);
                mRecyclerview.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
            }
        });
    }
}
