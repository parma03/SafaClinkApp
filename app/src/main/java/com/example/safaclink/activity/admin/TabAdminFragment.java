package com.example.safaclink.activity.admin;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.safaclink.R;
import com.example.safaclink.databinding.FragmentTabAdminBinding;
import com.example.safaclink.model.UserModel;

import java.util.List;

public class TabAdminFragment extends Fragment {
    private FragmentTabAdminBinding binding;
    private List<UserModel> userModelList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tab_admin, container, false);
    }
}