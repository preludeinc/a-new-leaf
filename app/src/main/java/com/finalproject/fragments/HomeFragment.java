package com.finalproject.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.finalproject.databinding.FragmentHomeBinding;

/**
 * Updates plant growth based on plant data updated in focus view model.
 */
public class HomeFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // view binding
        FragmentHomeBinding binding =
                FragmentHomeBinding.inflate(getLayoutInflater(), container, false);
        View homeFragmentView = binding.getRoot();
        ImageView plant = binding.imageViewPlant;

        Bundle bundle = getArguments();
        if (bundle != null) {
            // updates plant image based on ID determined in MainActivity,
            // with help taken from FocusViewModel class
            int ID = bundle.getInt("plant_id");
            plant.setImageResource(ID);
        }
        return homeFragmentView;
    }
}