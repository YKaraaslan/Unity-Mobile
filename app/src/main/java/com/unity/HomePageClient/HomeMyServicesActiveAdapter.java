package com.unity.HomePageClient;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.unity.R;

import java.util.List;

public class HomeMyServicesActiveAdapter extends RecyclerView.Adapter<HomeMyServicesActiveAdapter.RecyclerViewHolder> {
    List<HomeMyServicesActiveItems> contactList;
    HomeMyServicesActiveCallBack callBack;
    Context context;

    public HomeMyServicesActiveAdapter(Context context, List<HomeMyServicesActiveItems> contactList, HomeMyServicesActiveCallBack callBacks) {
        this.contactList = contactList;
        this.callBack = callBacks;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_home_my_services_recycler_view, parent, false);
        return new RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position) {
        holder.cardView.setAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_transition_animation));

        holder.image.setImageResource(contactList.get(position).getImage());
        holder.name.setText(contactList.get(position).getName());
        holder.description.setText(contactList.get(position).getDescription());
        holder.time.setText(contactList.get(position).getTime());
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    public class RecyclerViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name, description, time;
        CardView cardView;
        public RecyclerViewHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.contact_image);
            name = itemView.findViewById(R.id.contact_name);
            description = itemView.findViewById(R.id.contact_description);
            time = itemView.findViewById(R.id.contact_time);
            cardView = itemView.findViewById(R.id.contact_card);

            cardView.setOnClickListener(view -> {
                callBack.onItemClick(getAdapterPosition(), image, name, description, time);
            });
        }
    }
}
