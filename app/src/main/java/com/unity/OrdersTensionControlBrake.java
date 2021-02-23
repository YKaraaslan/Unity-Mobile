package com.unity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputLayout;

public class OrdersTensionControlBrake extends Fragment {
    TextInputLayout clipper;
    ConstraintLayout brakeTypeLayout;
    Chip magnetic, pneumatic;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_orders_tension_control_brake_fragment, container, false);

        clipper = view.findViewById(R.id.number_clipper_field);
        brakeTypeLayout = view.findViewById(R.id.brake_type_layout);
        magnetic = view.findViewById(R.id.chip_magnetic);
        pneumatic = view.findViewById(R.id.chip_pneumatic);

        if (magnetic.isChecked()){
            brakeTypeLayout.setVisibility(View.VISIBLE);
            clipper.setVisibility(View.GONE);
        }
        else if (pneumatic.isChecked()){
            brakeTypeLayout.setVisibility(View.GONE);
            clipper.setVisibility(View.VISIBLE);
        }

        magnetic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    brakeTypeLayout.setVisibility(View.VISIBLE);
                    clipper.setVisibility(View.GONE);
                }
            }
        });

        pneumatic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    brakeTypeLayout.setVisibility(View.GONE);
                    clipper.setVisibility(View.VISIBLE);
                }
            }
        });

        return view;
    }
}