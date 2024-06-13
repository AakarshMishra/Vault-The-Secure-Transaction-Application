package com.example.vault.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.example.vault.AddBankAccountActivity;
import com.example.vault.AddCardActivity;
import com.example.vault.KYCActivity;
import com.example.vault.LoginActivity;
import com.example.vault.MainActivity;
import com.example.vault.R;
import com.example.vault.SetUpiActivity;
import com.example.vault.ShowPhotoActivity;
import com.google.firebase.crashlytics.buildtools.reloc.javax.annotation.Nonnull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.scottyab.aescrypt.AESCrypt;

import java.security.GeneralSecurityException;
import java.util.concurrent.Executor;

public class AccountFragment extends Fragment {
    private Context context;
    private LinearLayout l,l1,l2, l3, l4;
    private String username;
    private TextView name;
    private TextView upi;
    private DatabaseReference reference;
    static AccountFragment fragment;
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    public static AccountFragment newInstance(String username) {
        fragment = new AccountFragment();
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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        l = view.findViewById(R.id.KYC);
        l1=view.findViewById(R.id.UPIB);
        l2=view.findViewById(R.id.showp);
        l3 = view.findViewById(R.id.bankAccountLayout);
        l4 = view.findViewById(R.id.cardAddLayout);
        name = view.findViewById(R.id.Name);
        upi = view.findViewById(R.id.upi);
        executor = ContextCompat.getMainExecutor(fragment.getContext());
        biometricPrompt=new BiometricPrompt((FragmentActivity) fragment.getContext(), executor,new BiometricPrompt.AuthenticationCallback(){
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString)
            {
                super.onAuthenticationError(errorCode,errString);

            }

            @Override
            public void onAuthenticationSucceeded(@Nonnull BiometricPrompt.AuthenticationResult result){
                super.onAuthenticationSucceeded(result);
                Intent i=new Intent(fragment.getContext(), ShowPhotoActivity.class);
                i.putExtra("username",username);
                startActivity(i);

            }

            @Override
            public void onAuthenticationFailed(){
                super.onAuthenticationFailed();

            }
        });

        l3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Open AddBankAccountActivity and pass the username
                Intent intent = new Intent(fragment.getContext(), AddBankAccountActivity.class);
                intent.putExtra("username", username);
                startActivity(intent);
            }
        });

        l4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Open AddBankAccountActivity and pass the username
                Intent intent = new Intent(fragment.getContext(), AddCardActivity.class);
                intent.putExtra("username", username);
                startActivity(intent);
            }
        });

        promptInfo=new BiometricPrompt.PromptInfo.Builder().setTitle("Biometric Authentication").setSubtitle("Login using Fingerprint or face").setNegativeButtonText("Cancel").build();

        l.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(fragment.getContext(), KYCActivity.class);
                i.putExtra("username",username);
                startActivity(i);
            }
        });
        l1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(fragment.getContext(), SetUpiActivity.class);
                i.putExtra("username",username);
                startActivity(i);
            }
        });
        l2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                biometricPrompt.authenticate(promptInfo);
            }
        });


        reference = FirebaseDatabase.getInstance().getReference("users");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(username)) {
                    try {
                        name.setText(AESCrypt.decrypt("Vault", dataSnapshot.child(username).child("name").getValue(String.class)));
                    } catch (GeneralSecurityException e) {
                        e.printStackTrace();
                    }
                    upi.setText(dataSnapshot.child(username).child("username").getValue(String.class) + "@vault");
                } else {
                    // Invalid Teacher ID, show error message
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle onCancelled as needed
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }
}
