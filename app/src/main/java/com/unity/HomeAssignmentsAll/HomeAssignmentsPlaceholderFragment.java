package com.unity.HomeAssignmentsAll;

import android.os.Bundle;

import androidx.fragment.app.Fragment;


public class HomeAssignmentsPlaceholderFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    public static HomeAssignmentsPlaceholderFragment newInstance(int index) {
        HomeAssignmentsPlaceholderFragment fragment = new HomeAssignmentsPlaceholderFragment();
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