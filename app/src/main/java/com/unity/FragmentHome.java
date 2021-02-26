package com.unity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;
import com.unity.HomeAssignmentsAll.HomeAssignmentsAll;
import com.unity.HomePageClient.CurrentActivity;
import com.unity.HomePageClient.HomeFinance;
import com.unity.HomePageClient.HomeMyAssignments;
import com.unity.HomePageClient.HomeMyOffers;
import com.unity.HomePageClient.HomeMyOrders;
import com.unity.HomePageClient.HomeMyServices;
import com.unity.HomePageClient.HomeOrders;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static android.content.Context.MODE_PRIVATE;

public class FragmentHome extends Fragment {
    private static final String KEY_UPDATE_URL = "updateVisitApk";

    Toolbar toolbar;
    SliderView sliderView;
    List<SliderItem> imageList;
    CardView serviceCard, orderCard, assignment_card, my_services_card, my_offers_card, my_orders_card, my_assignments_card, user_on_hold, finance_card, assignments_all_card;
    CardView assignments_completed_card;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor sharedEditor;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    ConstraintLayout create_assignment_layout;
    FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
    SliderAdapter adapter;
    List<String> photoList = new ArrayList<>();
    List<String> descriptionList = new ArrayList<>();
    ProgressBar progressBar;


    @SuppressLint("CommitPrefEdits")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        sliderView = view.findViewById(R.id.imageSlider);

        serviceCard = view.findViewById(R.id.service_card);
        orderCard = view.findViewById(R.id.order_card);
        assignment_card = view.findViewById(R.id.assignment_card);
        my_services_card = view.findViewById(R.id.my_services_card);
        my_offers_card = view.findViewById(R.id.my_offers_card);
        my_orders_card = view.findViewById(R.id.my_orders_card);
        my_assignments_card = view.findViewById(R.id.my_assignments_card);
        user_on_hold = view.findViewById(R.id.user_on_hold);
        create_assignment_layout = view.findViewById(R.id.create_assignment_layout);
        finance_card = view.findViewById(R.id.finance_card);
        assignments_all_card = view.findViewById(R.id.assignments_all_card);
        assignments_completed_card = view.findViewById(R.id.assignments_completed_card);
        progressBar = view.findViewById(R.id.progressBar);

        sharedPreferences = requireActivity().getSharedPreferences("Log", MODE_PRIVATE);
        sharedEditor = sharedPreferences.edit();

        imageList = new ArrayList<>();
        init();
        CurrentActivity.setCurrectActivity("FragmentHome");

        return view;
    }

    private void init() {
        db.collection("BannerPhotos").addSnapshotListener((value, error) -> {
            if (value != null){
                photoList.clear();
                descriptionList.clear();
                for (DocumentChange documentChange : value.getDocumentChanges()){
                    DocumentSnapshot documentSnapshot = documentChange.getDocument();
                    photoList.add(documentSnapshot.getString("name"));
                    descriptionList.add(documentSnapshot.getString("description"));
                }

                final long ONE_MEGABYTE = 1024 * 1024;
                for (int i = 0; i < photoList.size(); i++) {
                    int finalI = i;
                    storageReference.child("Banner").child(photoList.get(i)).getBytes(ONE_MEGABYTE).addOnSuccessListener(bytes -> {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        imageList.add(new SliderItem(bitmap, descriptionList.get(finalI)));
                        adapter.notifyDataSetChanged();
                        progressBar.setVisibility(View.GONE);
                    }).addOnFailureListener(e -> progressBar.setVisibility(View.GONE));
                }
            }
        });

        adapter = new SliderAdapter(getContext(), imageList);
        sliderView.setSliderAdapter(adapter);
        sliderView.setIndicatorAnimation(IndicatorAnimationType.WORM);
        sliderView.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
        sliderView.startAutoCycle();

        serviceCard.setOnClickListener(view -> startActivity(new Intent(getContext(), Services.class)));

        orderCard.setOnClickListener(view -> startActivity(new Intent(getContext(), HomeOrders.class)));

        assignment_card.setOnClickListener(view -> startActivity(new Intent(getContext(), AssignmentsCreate.class)));

        // TODO: 27/08/2020 HomeMyServices Yapilacak
        my_services_card.setOnClickListener(view -> {
            int number = new Random().nextInt(4);

            if (number >= 2) {
                startActivity(new Intent(getContext(), HomeMyServices.class));
            } else {
                startActivity(new Intent(getContext(), HomeMyServicesOurs.class));
            }
        });

        // TODO: 27/08/2020 HomeMyOffers Yapilacak 
        my_offers_card.setOnClickListener(view -> startActivity(new Intent(getContext(), HomeMyOffers.class)));

        // TODO: 27/08/2020 HomeMyOrders Yapilacak
        my_orders_card.setOnClickListener(view -> startActivity(new Intent(getContext(), HomeMyOrders.class)));

        my_assignments_card.setOnClickListener(view -> startActivity(new Intent(getContext(), HomeMyAssignments.class)));

        user_on_hold.setOnClickListener(view -> startActivity(new Intent(getContext(), UsersOnHold.class)));

        finance_card.setOnClickListener(view -> startActivity(new Intent(requireContext(), HomeFinance.class)));

        orderCard.setOnClickListener(view -> startActivity(new Intent(getContext(), HomeOrders.class)));

        assignments_completed_card.setOnClickListener(view -> startActivity(new Intent(getContext(), AssignmentsTotallyFinished.class)));

        assignments_all_card.setOnClickListener(view -> {
            Intent intent = new Intent(getContext(), HomeAssignmentsAll.class);
            intent.putExtra("all", true);
            startActivity(intent);
        });

        if (sharedPreferences.getBoolean("assignment_create", false)){
            create_assignment_layout.setVisibility(View.VISIBLE);
        }

        if (sharedPreferences.getBoolean("assignments_all", false)){
            assignments_all_card.setVisibility(View.VISIBLE);
            assignments_completed_card.setVisibility(View.VISIBLE);
        }

        if (sharedPreferences.getBoolean("user_on_hold", false)){
            user_on_hold.setVisibility(View.VISIBLE);
        }

        if(sharedPreferences.getBoolean("finance", false)){
            finance_card.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.home_page, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        item.setVisible(false);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share:
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/html");
                String title = getString(R.string.invitation_message) + "\n\n" + mFirebaseRemoteConfig.getString(KEY_UPDATE_URL);
                share.putExtra(Intent.EXTRA_TEXT, title);
                startActivity(share);
                break;
            case R.id.action_info:
                Toast.makeText(requireActivity(), "Yakında aktifleşecektir.", Toast.LENGTH_SHORT).show();
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
