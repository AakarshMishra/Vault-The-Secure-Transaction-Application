package com.example.vault.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.vault.MakePaymentActivity;
import com.example.vault.R;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.squareup.picasso.Picasso;

public class PaymentFragment extends Fragment {

    private Context context;
    private TabLayout mTabLayout;
    private ImageView qrCodeImageView;
    private DatabaseReference databaseReference;
    private String username;
    static PaymentFragment fragment;
    public PaymentFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    public static PaymentFragment newInstance(String username) {
        fragment = new PaymentFragment();
        Bundle args = new Bundle();
        args.putString("username", username);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            username = getArguments().getString("username");
        }
    }

    private void initViews(View view) {
        mTabLayout = view.findViewById(R.id.tab_payment);
        mTabLayout.addTab(mTabLayout.newTab().setText("POS"));
        mTabLayout.addTab(mTabLayout.newTab().setText("SCAN QR"));
        mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        mTabLayout.getTabAt(0).select();
        String username = getArguments().getString("username");
        if (username != null && !username.isEmpty()) {
            loadQRCodeImage(username);
        }
        qrCodeImageView = view.findViewById(R.id.qr_code_image_view);
    }

    private void loadQRCodeImage(String username) {
        databaseReference = FirebaseDatabase.getInstance().getReference("kyc_data").child(username).child("barcodeImageUrl");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String imageUrl = dataSnapshot.getValue(String.class);
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Picasso.get().load(imageUrl).into(qrCodeImageView);
                    qrCodeImageView.setVisibility(View.VISIBLE);
                } else {
                    // Placeholder image
                    qrCodeImageView.setImageResource(R.drawable.notfound);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                qrCodeImageView.setImageResource(R.drawable.notfound);
                Toast.makeText(context, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startQRCodeScanner() {
        IntentIntegrator integrator = IntentIntegrator.forSupportFragment(this);
        integrator.setPrompt("Scan a QR Code");
        integrator.setOrientationLocked(false);
        integrator.initiateScan();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_payment, container, false);
        initViews(view);

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (position == 0) {
                    Toast.makeText(context, "POS selected", Toast.LENGTH_SHORT).show();
                    String username = getArguments().getString("username");
                    if (username != null && !username.isEmpty()) {
                        loadQRCodeImage(username);
                    }
                } else {
                    startQRCodeScanner();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                Toast.makeText(context, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                Intent i = new Intent(fragment.getContext(), MakePaymentActivity.class);
                i.putExtra("receiverUPI", result.getContents());
                i.putExtra("username", username);
                startActivity(i);
                // You can handle the scanned data here
            } else {
                Toast.makeText(context, "Cancelled", Toast.LENGTH_LONG).show();
            }
        }
    }
}
