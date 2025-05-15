package com.example.safaclink.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.safaclink.activity.admin.TabAdminFragment;
import com.example.safaclink.activity.admin.TabKonsumenFragment;
import com.example.safaclink.activity.admin.TabOwnerFragment;

public class TabUserAdapter extends FragmentStateAdapter {
    public TabUserAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new TabAdminFragment();
            case 1:
                return new TabKonsumenFragment();
//            case 2:
//                return new TabOwnerFragment();
            default:
                return new TabAdminFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
