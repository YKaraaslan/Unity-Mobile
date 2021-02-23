package com.unity;

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

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.unity.HomePageClient.HomeMyAssignmentsItems;

import java.util.ArrayList;
import java.util.List;

public class ListOfPeopleAdapter extends RecyclerView.Adapter<ListOfPeopleAdapter.RecyclerViewHolder> {

    List<ListOfPeopleItems> workersList;
    ListOfPeopleCallBack callBack;
    Context context;
    StorageReference storageReference = FirebaseStorage.getInstance().getReference();

    public ListOfPeopleAdapter(Context context, List<ListOfPeopleItems> workersList, ListOfPeopleCallBack callBacks) {
        this.workersList = workersList;
        this.callBack = callBacks;
        this.context = context;
    }

    @NonNull
    @Override
    public ListOfPeopleAdapter.RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_of_people_recyclerview, parent, false);
        return new ListOfPeopleAdapter.RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListOfPeopleAdapter.RecyclerViewHolder holder, int position) {
        //holder.cardView.setAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_transition_animation));
        holder.name.setText(workersList.get(position).getName());

        storageReference.child("Users").child(String.valueOf(workersList.get(position).getId())).getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(context)
                    .load(uri.toString())
                    .centerCrop()
                    .into(holder.image);

        }).addOnFailureListener(e -> holder.image.setImageResource(R.drawable.unity));
    }

    public void filterList(ArrayList<ListOfPeopleItems> filteredList) {
        workersList = filteredList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return workersList.size();
    }

    public class RecyclerViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name;
        CardView cardView;
        public RecyclerViewHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.workers_image);
            name = itemView.findViewById(R.id.workers_name);
            cardView = itemView.findViewById(R.id.workers_card);

            cardView.setOnClickListener(view -> {
                callBack.onItemClick(getAdapterPosition(), workersList.get(getAdapterPosition()).getId(), workersList.get(getAdapterPosition()).getName());
            });
        }
    }
}
