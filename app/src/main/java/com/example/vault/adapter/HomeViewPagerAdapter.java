package com.example.vault.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.example.vault.R;

import java.util.ArrayList;

public class HomeViewPagerAdapter extends PagerAdapter {

    Context context;
    ArrayList<String>offersList;

    public HomeViewPagerAdapter(Context context,ArrayList<String>offersList){
        this.context=context;
        this.offersList=offersList;

    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_layout_offers_home,container,false);
        TextView txvOffer= (TextView)view.findViewById(R.id.txv_home_lay_1);
        txvOffer.setText(offersList.get(position));
        container.addView(view);
        return view;
    }

    @Override
    public int getCount() {
        return offersList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {

        return view == object;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        View view =(View)object;
        container.removeView(view);
    }
}
