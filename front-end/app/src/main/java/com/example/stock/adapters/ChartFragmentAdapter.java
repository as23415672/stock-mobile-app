package com.example.stock.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.stock.fragments.ChartFragment;

import java.util.List;

public class ChartFragmentAdapter extends FragmentStateAdapter {

    private final List<String> urls;

    public ChartFragmentAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle, List<String> urls) {
        super(fragmentManager, lifecycle);
        this.urls = urls;
    }
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return ChartFragment.newInstance(urls.get(position));
    }

    @Override
    public int getItemCount() {
        return urls.size();
    }
}
