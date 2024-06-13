package com.example.vault.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.vault.R;
import com.example.vault.AddMoneyActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private Context context;
    private String username;
    private static HomeFragment fragment;
    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance(String username) {
        fragment = new HomeFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        LinearLayout layBusss = view.findViewById(R.id.lay_busss);
        layBusss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirect to AddMoneyActivity with username
                Intent intent = new Intent(fragment.getContext(), AddMoneyActivity.class);
                intent.putExtra("username", username);
                startActivity(intent);
            }
        });

        return view;
    }
}
