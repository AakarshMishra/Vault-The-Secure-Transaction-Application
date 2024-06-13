package com.example.vault;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.vault.R;
import com.example.vault.fragment.AccountFragment;
import com.example.vault.fragment.HomeFragment;
import com.example.vault.fragment.OffersFragment;
import com.example.vault.fragment.PaymentFragment;
import com.example.vault.fragment.TransactionsFragment;
import com.example.vault.helper.BottomNavigationViewHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.crashlytics.buildtools.reloc.javax.annotation.Nonnull;

import java.util.concurrent.Executor;

public class HomeActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private TextView mTxvToolbarTitle;
    private BottomNavigationView mBottomNavigationView;
    private HomeFragment homeFragment;
    private OffersFragment offersFragment;
    private PaymentFragment paymentFragment;
    private TransactionsFragment transactionsFragment;
    private String username;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private Executor executor;

    // AccountFragment instance
    private AccountFragment accountFragment;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        if (item.getItemId() == R.id.navigation_home) {
            mTxvToolbarTitle.setText(R.string.app_name);
            homeFragment = homeFragment.newInstance(username);
            setUpFragment(homeFragment);
            return true;
        } else if (item.getItemId() == R.id.navigation_offers) {
            mTxvToolbarTitle.setText(R.string.title_offers);
            setUpFragment(offersFragment);
            return true;
        } else if (item.getItemId() == R.id.navigation_payment) {
            mTxvToolbarTitle.setText(R.string.title_payment);
            paymentFragment = PaymentFragment.newInstance(username);
            setUpFragment(paymentFragment);
            return true;
        } else if (item.getItemId() == R.id.navigation_account) {
            mTxvToolbarTitle.setText(R.string.title_my_account);
            // Update the AccountFragment instance with username
            accountFragment = AccountFragment.newInstance(username);
            setUpFragment(accountFragment);
            return true;
        } else if (item.getItemId() == R.id.navigation_transactions) {
            biometricPrompt.authenticate(promptInfo);
            return true;
        } else {
            return false;
        }
    };

    public void initViews() {
        setContentView(R.layout.activity_home);
        mToolbar = findViewById(R.id.toolbar);
        mTxvToolbarTitle = findViewById(R.id.txv_toolbar_title);
        mBottomNavigationView = findViewById(R.id.navigation);
        homeFragment = new HomeFragment();
        offersFragment = OffersFragment.newInstance();
//        paymentFragment = PaymentFragment.newInstance();
//        transactionsFragment = TransactionsFragment.newInstance();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
        mTxvToolbarTitle.setText(R.string.app_name);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("");
        Intent i = getIntent();
        username = i.getStringExtra("username");
        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt=new BiometricPrompt(HomeActivity.this, executor,new BiometricPrompt.AuthenticationCallback(){
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString)
            {
                super.onAuthenticationError(errorCode,errString);

            }

            @Override
            public void onAuthenticationSucceeded(@Nonnull BiometricPrompt.AuthenticationResult result){
                super.onAuthenticationSucceeded(result);
                mTxvToolbarTitle.setText(R.string.title_transactions);
                transactionsFragment = transactionsFragment.newInstance(username);
                setUpFragment(transactionsFragment);

            }

            @Override
            public void onAuthenticationFailed(){
                super.onAuthenticationFailed();

            }
        });

        promptInfo=new BiometricPrompt.PromptInfo.Builder().setTitle("Biometric Authentication").setSubtitle("Login using Fingerprint or face").setNegativeButtonText("Cancel").build();



        mBottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        BottomNavigationViewHelper.removeShiftMode(mBottomNavigationView);

        // Initially set the home fragment
        homeFragment = homeFragment.newInstance(username);
        setUpFragment(homeFragment);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_invite) {
            Toast.makeText(this, "Invite and Earn", Toast.LENGTH_SHORT).show();
            return true;
        } else if (item.getItemId() == R.id.menu_notification) {
            Toast.makeText(this, "Notification", Toast.LENGTH_SHORT).show();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }

    }

    private void setUpFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
        fragmentTransaction.replace(R.id.container_home, fragment);
        fragmentTransaction.commit();
    }
}
