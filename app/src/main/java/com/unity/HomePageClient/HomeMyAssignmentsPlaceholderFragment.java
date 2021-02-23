package com.unity.HomePageClient;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

public class HomeMyAssignmentsPlaceholderFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    public static HomeMyAssignmentsPlaceholderFragment newInstance(int index) {
        HomeMyAssignmentsPlaceholderFragment fragment = new HomeMyAssignmentsPlaceholderFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}