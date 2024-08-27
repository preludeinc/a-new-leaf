package com.finalproject.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.finalproject.AccessSharedPref;
import com.finalproject.databinding.FragmentStatsBinding;

/**
 * Accesses data updated in Receiver class and stored in shared preferences.
 * Shows today's focus session stats.
 */
public class StatsFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentStatsBinding binding = FragmentStatsBinding.inflate(getLayoutInflater(), container,
                false);
        View statsView = binding.getRoot();
        TextView dailyFocusTime = binding.statsTextTotals;
        TextView dailyFocusSessions = binding.plantsText;
        // accesses shared preferences from helper class
        AccessSharedPref sharedPref = new AccessSharedPref(requireContext());
        // compares stored date (from shared preferences) and current date
        // stats are updated if they match
        if (sharedPref.compareDates()) {
            String updatedTime = sharedPref.getFocusTime();
            String updatedFocusSessions = sharedPref.getFocusSessions();
            updateText(dailyFocusTime, updatedTime);
            updateText(dailyFocusSessions, updatedFocusSessions);
            // if it's a different day, stats are reset
        } else {
            String resetStats = "0";
            updateText(dailyFocusTime, resetStats);
            updateText(dailyFocusSessions, resetStats);
        }
        return statsView;
    }

    /**
     * Helper function to update text for Focus Sessions and Number of Plants grown.
     *
     * @param textViewToUpdate textView field where text will be updated.
     * @param fieldWithData    String used to help update textView.
     */
    private void updateText(TextView textViewToUpdate, String fieldWithData) {
        textViewToUpdate.setText(fieldWithData);
    }
}