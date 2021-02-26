package com.unity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.List;

public class UsersOnHoldAdapter extends RecyclerView.Adapter<UsersOnHoldAdapter.RecyclerViewHolder> {
    List<UsersOnHoldItems> contactList;
    UsersOnHoldCallBack callBack;
    Context context;
    StorageReference storageReference = FirebaseStorage.getInstance().getReference();

    public UsersOnHoldAdapter(Context context, List<UsersOnHoldItems> contactList, UsersOnHoldCallBack callBacks) {
        this.contactList = contactList;
        this.callBack = callBacks;
        this.context = context;
    }

    @NonNull
    @Override
    public UsersOnHoldAdapter.RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_of_people_recyclerview, parent, false);
        return new UsersOnHoldAdapter.RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsersOnHoldAdapter.RecyclerViewHolder holder, int position) {
        holder.name.setText(contactList.get(position).getName());

        storageReference.child("UsersOnHold").child(String.valueOf(contactList.get(position).getId())).getDownloadUrl().addOnSuccessListener(uri -> Glide.with(context)
                .load(uri.toString())
                .centerCrop()
                .into(holder.image)).addOnFailureListener(e -> holder.image.setImageResource(R.drawable.unity));
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    public class RecyclerViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        CardView cardView;
        CircularImageView image;

        public RecyclerViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.workers_name);
            cardView = itemView.findViewById(R.id.workers_card);
            image = itemView.findViewById(R.id.workers_image);

            cardView.setOnClickListener(view -> callBack.onItemClick(getAdapterPosition()));
        }
    }
}
