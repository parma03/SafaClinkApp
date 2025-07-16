package com.example.safaclink.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.safaclink.activity.konsumen.TabDikerjakanKonsumenFragment;
import com.example.safaclink.activity.konsumen.TabKonfirmasiKonsumenFragment;
import com.example.safaclink.activity.konsumen.TabOrdersKonsumenFragment;

public class TabOrderKonsumen extends FragmentStateAdapter {
    public TabOrderKonsumen(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new TabOrdersKonsumenFragment();
            case 1:
                return new TabDikerjakanKonsumenFragment();
            case 2:
                return new TabKonfirmasiKonsumenFragment();
            default:
                return new TabOrdersKonsumenFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
