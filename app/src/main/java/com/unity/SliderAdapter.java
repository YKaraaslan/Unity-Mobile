package com.unity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.smarteist.autoimageslider.SliderViewAdapter;

import java.util.List;

public class SliderAdapter extends SliderViewAdapter<SliderAdapter.SliderViewHolder> {

    Context context;
    List<SliderItem> imageList;

    public SliderAdapter(Context context, List<SliderItem> imageList) {
        this.context = context;
        this.imageList = imageList;
    }
    @Override
    public SliderViewHolder onCreateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.slider_item_layout, parent, false);
        return new SliderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SliderViewHolder viewHolder, int position) {
        viewHolder.sliderImageView.setImageBitmap(imageList.get(position).getImage());
        viewHolder.description.setText(imageList.get(position).getDescription());
        if (imageList.get(position).getDescription().equals("")){
            viewHolder.description.setVisibility(View.GONE);
        }
    }

    @Override
    public int getCount() {
        return imageList.size();
    }

    public static class SliderViewHolder extends SliderViewAdapter.ViewHolder {
        ImageView sliderImageView;
        TextView description;

        public SliderViewHolder(View itemView) {
            super(itemView);
            sliderImageView = itemView.findViewById(R.id.slider_image);
            description = itemView.findViewById(R.id.description);
        }
    }
}

