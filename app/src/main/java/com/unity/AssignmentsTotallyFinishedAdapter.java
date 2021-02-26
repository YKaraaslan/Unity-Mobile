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
import com.unity.HomePageClient.HomeMyAssignmentsItems;

import java.util.ArrayList;
import java.util.List;

public class AssignmentsTotallyFinishedAdapter extends RecyclerView.Adapter<AssignmentsTotallyFinishedAdapter.RecyclerViewHolder> {
    List<HomeMyAssignmentsItems> contactList;
    AssignmentsTotallyFinishedCallBack callBack;
    Context context;
    StorageReference storageReference = FirebaseStorage.getInstance().getReference();

    public AssignmentsTotallyFinishedAdapter(Context context, List<HomeMyAssignmentsItems> contactList, AssignmentsTotallyFinishedCallBack callBacks) {
        this.contactList = contactList;
        this.callBack = callBacks;
        this.context = context;
    }

    @NonNull
    @Override
    public AssignmentsTotallyFinishedAdapter.RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_home_my_assignments_active_recycler_view, parent, false);
        return new AssignmentsTotallyFinishedAdapter.RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position) {
        holder.description.setText(contactList.get(position).getNote());

        storageReference.child("Users").child(String.valueOf(contactList.get(position).getInChargeID())).getDownloadUrl().addOnSuccessListener(uri -> Glide.with(context)
                .load(uri.toString())
                .centerCrop()
                .into(holder.image)).addOnFailureListener(e -> holder.image.setImageResource(R.drawable.unity));

        String lastTitle = "(" + contactList.get(position).getId() + ") - " + contactList.get(position).getTitle();
        if (lastTitle.length() > 35){
            lastTitle = lastTitle.substring(0, 35) + "...";
        }
        holder.name.setText(lastTitle);

        String dateTime = contactList.get(position).getDate() + " " + contactList.get(position).getTime();
        DateFormatter dateFormatter = new DateFormatter();
        String convTime = dateFormatter.format(dateTime);
        holder.time.setText(convTime);
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

            cardView.setOnClickListener(view -> callBack.onItemClick(getAdapterPosition()));
        }
    }

    public void filterList(ArrayList<HomeMyAssignmentsItems> filteredList) {
        contactList = filteredList;
        notifyDataSetChanged();
    }
}
