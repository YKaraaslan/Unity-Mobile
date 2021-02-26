package com.unity.ProductsClient;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.unity.R;

import java.util.List;

public class CableProductInformationAdapter extends RecyclerView.Adapter<CableProductInformationAdapter.RecyclerViewHolder> {
    List<CableProductInformationItems> productInformationItemsList;
    Context context;
    CableProductInformationCallBack callBack;

    public CableProductInformationAdapter(Context context, List<CableProductInformationItems> productInformationItemsList, CableProductInformationCallBack callBack){
        this.productInformationItemsList = productInformationItemsList;
        this.context = context;
        this.callBack = callBack;
    }

    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_product_information_recyclerview, parent, false);
        return new CableProductInformationAdapter.RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position) {
        holder.image.setImageResource(productInformationItemsList.get(position).getThumbnail());
        holder.title.setText(productInformationItemsList.get(position).getTitle());
    }

    @Override
    public int getItemCount() {
        return productInformationItemsList.size();
    }

    public class RecyclerViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title;
        MaterialCardView cardView;
        public RecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            title = itemView.findViewById(R.id.title);
            cardView = itemView.findViewById(R.id.cardView);

            cardView.setOnClickListener(view -> callBack.onItemClick(getAdapterPosition(), image, title));
        }
    }
}
