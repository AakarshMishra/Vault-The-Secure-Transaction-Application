package com.example.vault;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
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

public class AddCardActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private CardAdapter adapter;
    private List<Card> cardList;
    private DatabaseReference databaseRef;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_card);

        Intent intent = getIntent();
        username = intent.getStringExtra("username");

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        cardList = new ArrayList<>();
        adapter = new CardAdapter(cardList);
        recyclerView.setAdapter(adapter);

        // Add this code inside onCreate method after recyclerView setup

        FloatingActionButton fabAddBankAccount = findViewById(R.id.fabAddCardAccount);
        fabAddBankAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newBankAddIntent = new Intent(AddCardActivity.this, NewCardAddActivity.class);
                newBankAddIntent.putExtra("username", username);
                startActivity(newBankAddIntent);
            }
        });

        // Connect to Firebase and fetch card data
        databaseRef = FirebaseDatabase.getInstance().getReference("Cards").child(username);
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                cardList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Card card = snapshot.getValue(Card.class);
                    if (card != null) {
                        cardList.add(card);
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

    class CardAdapter extends RecyclerView.Adapter<CardAdapter.ViewHolder> {
        private List<Card> cardList;

        public CardAdapter(List<Card> cardList) {
            this.cardList = cardList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bank_card_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Card card = cardList.get(position);
            try {
                holder.tvName.setText("CARD HOLDER NAME: "+AESCrypt.decrypt("Vault",card.getName()));
            } catch (GeneralSecurityException e) {
                throw new RuntimeException(e);
            }
            try {
                holder.tvExpiryDate.setText("EXPIRY DATE: "+AESCrypt.decrypt("Vault",card.getExpiryDate()));
            } catch (GeneralSecurityException e) {
                throw new RuntimeException(e);
            }
            try {
                holder.tvCVV.setText("CVV: "+AESCrypt.decrypt("Vault",card.getCvv()));
            } catch (GeneralSecurityException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public int getItemCount() {
            return cardList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName;
            TextView tvExpiryDate;
            TextView tvCVV;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvName);
                tvExpiryDate = itemView.findViewById(R.id.tvExpiryDate);
                tvCVV = itemView.findViewById(R.id.tvCVV);
            }
        }
    }
}

class Card {
    private String name;
    private String expiryDate;
    private String cvv;

    public Card() {
        // Default constructor required for Firebase's getValue method
    }

    public Card(String name, String expiryDate, String cvv) {
        this.name = name;
        this.expiryDate = expiryDate;
        this.cvv = cvv;
    }

    // Getters and setters for all attributes

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    // Override toString() method for debugging or logging purposes

    @Override
    public String toString() {
        return "Card{" +
                "name='" + name + '\'' +
                ", expiryDate='" + expiryDate + '\'' +
                ", cvv='" + cvv + '\'' +
                '}';
    }
}