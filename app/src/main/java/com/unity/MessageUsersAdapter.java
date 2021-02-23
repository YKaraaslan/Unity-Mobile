package com.unity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.unity.HomePageClient.HomeMyAssignmentsItems;

import java.util.ArrayList;
import java.util.List;

public class MessageUsersAdapter extends RecyclerView.Adapter<MessageUsersAdapter.RecyclerViewHolder> {
    List<MessageUsersItem> contactList;
    MessageUsersCallBack callBack;
    Context context;
    StorageReference storageReference = FirebaseStorage.getInstance().getReference();

    public MessageUsersAdapter(Context context, List<MessageUsersItem> contactList, MessageUsersCallBack callBacks) {
        this.contactList = contactList;
        this.callBack = callBacks;
        this.context = context;
    }

    @NonNull
    @Override
    public MessageUsersAdapter.RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_home_my_assignments_recycler_view, parent, false);
        return new MessageUsersAdapter.RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageUsersAdapter.RecyclerViewHolder holder, int position) {
        holder.name.setText(contactList.get(position).getName());
        holder.description.setText(contactList.get(position).getDescription());
        holder.time.setVisibility(View.GONE);
        /*String dateTime = contactList.get(position).getOnline();
        DateFormatter dateFormatter = new DateFormatter();
        String convTime = dateFormatter.format(dateTime);
        holder.time.setText(convTime);*/

        storageReference.child("Users").child(String.valueOf(contactList.get(position).getId())).getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(context)
                    .load(uri.toString())
                    .centerCrop()
                    .into(holder.image);
        }).addOnFailureListener(e -> holder.image.setImageResource(R.drawable.unity));
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    public class RecyclerViewHolder extends RecyclerView.ViewHolder {
        CircularImageView image;
        TextView name, description, time;
        CardView cardView;

        public RecyclerViewHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.contact_image);
            name = itemView.findViewById(R.id.contact_name);
            description = itemView.findViewById(R.id.contact_description);
            time = itemView.findViewById(R.id.contact_time);
            cardView = itemView.findViewById(R.id.contact_card);

            cardView.setOnClickListener(view -> callBack.onItemClick(getAdapterPosition(), contactList.get(getAdapterPosition()).getId(),
                    contactList.get(getAdapterPosition()).getName(), contactList.get(getAdapterPosition()).getDescription()));
        }
    }

    public void filterList(ArrayList<MessageUsersItem> filteredList) {
        contactList = filteredList;
        notifyDataSetChanged();
    }
}
