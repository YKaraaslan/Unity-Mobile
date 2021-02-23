package com.unity.HomePageClient;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.unity.R;

import java.util.ArrayList;
import java.util.List;

public class HomeMyServicesActive extends Fragment implements HomeMyServicesActiveCallBack {
    RecyclerView recyclerView;
    HomeMyServicesActiveAdapter adapter;
    List<HomeMyServicesActiveItems> list = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_my_services_pager_active_services, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        setupRecyclerView();
        init();
        return view;
    }

    private void init() {
        for (int i = 0; i < 10; i++) {
            list.add(new HomeMyServicesActiveItems(i, "Aktif Servisler", "Aciklama", "13:51", R.drawable.unity));
        }
    }

    private void setupRecyclerView() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setItemViewCacheSize(50);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new HomeMyServicesActiveAdapter(requireContext(), list, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(int position, ImageView imageContainer, TextView name, TextView description, TextView time) {

    }
}