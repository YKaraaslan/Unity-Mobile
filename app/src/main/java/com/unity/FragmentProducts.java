package com.unity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.firebase.messaging.FirebaseMessaging;
import com.unity.HomePageClient.CurrentActivity;
import com.unity.ProductsClient.AutomationControl;
import com.unity.ProductsClient.Cable;
import com.unity.ProductsClient.Camera;
import com.unity.ProductsClient.TensionControl;
import com.unity.ProductsClient.WebGuide;

import static android.content.Context.MODE_PRIVATE;

public class FragmentProducts extends Fragment {
    Toolbar toolbar;
    CardView webGuide, tensionControl, camera, cable, automationControl;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor sharedEditor;

    @SuppressLint("CommitPrefEdits")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_products, container, false);
        toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        webGuide = view.findViewById(R.id.webGuide);
        tensionControl = view.findViewById(R.id.tensionControl);
        camera = view.findViewById(R.id.camera);
        cable = view.findViewById(R.id.cable);
        automationControl = view.findViewById(R.id.automationControl);
        sharedPreferences = requireActivity().getSharedPreferences("Log", MODE_PRIVATE);
        sharedEditor = sharedPreferences.edit();
        init();
        CurrentActivity.setCurrectActivity("FragmentProducts");
        return view;
    }

    private void init() {
        webGuide.setOnClickListener(view -> startActivity(new Intent(getContext(), WebGuide.class)));

        tensionControl.setOnClickListener(view -> startActivity(new Intent(getContext(), TensionControl.class)));

        camera.setOnClickListener(view -> startActivity(new Intent(getContext(), Camera.class)));

        cable.setOnClickListener(view -> startActivity(new Intent(getContext(), Cable.class)));

        automationControl.setOnClickListener(view -> startActivity(new Intent(getContext(), AutomationControl.class)));
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.home_page, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Toast.makeText(requireContext(), "searcsh", Toast.LENGTH_SHORT).show();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_share:
                break;
            case R.id.action_reminder:
                startActivity(new Intent(getContext(), Reminder.class));
                break;
            case R.id.action_logout:
                AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
                builder.setMessage(getString(R.string.are_you_sure_to_exit));
                builder.setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                    sharedEditor.clear().apply();
                    Unsubscribe();
                    startActivity(new Intent(getContext(), MainActivity.class));
                    requireActivity().finish();
                });
                builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> {

                });
                AlertDialog alert = builder.create();
                alert.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void Unsubscribe() {
        FirebaseMessaging.getInstance().unsubscribeFromTopic("finance")
                .addOnCompleteListener(task -> Log.d("HOMEPAGE", "UNSUBSCRIBED"));
    }
}
