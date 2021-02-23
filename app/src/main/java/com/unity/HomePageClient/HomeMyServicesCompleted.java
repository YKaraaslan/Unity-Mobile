package com.unity.HomePageClient;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.unity.R;

import java.util.ArrayList;
import java.util.List;

public class HomeMyServicesCompleted extends Fragment implements HomeMyServicesCompletedCallBack {
    RecyclerView recyclerView;
    HomeMyServicesCompletedAdapter adapter;
    List<HomeMyServicesCompletedItems> list = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_my_services_pager_completed_services, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        setupRecyclerView();
        init();
        return view;
    }

    private void init() {
        for (int i = 0; i < 10; i++) {
            list.add(new HomeMyServicesCompletedItems(i, "Tamamlanan Servisler", "Aciklama", "13:51", R.drawable.unity));
        }
    }

    private void setupRecyclerView() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setItemViewCacheSize(50);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new HomeMyServicesCompletedAdapter(requireContext(), list, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(int position, ImageView imageContainer, TextView name, TextView description, TextView time) {

    }
}