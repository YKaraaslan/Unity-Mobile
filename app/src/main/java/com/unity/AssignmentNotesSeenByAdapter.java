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

import java.util.List;

public class AssignmentNotesSeenByAdapter extends RecyclerView.Adapter<AssignmentNotesSeenByAdapter.RecyclerViewHolder> {
    List<AssignmentNotesSeenByItems> contactList;
    AssignmentNotesSeenByCallBack callBack;
    Context context;
    StorageReference storageReference = FirebaseStorage.getInstance().getReference();

    public AssignmentNotesSeenByAdapter(Context context, List<AssignmentNotesSeenByItems> contactList, AssignmentNotesSeenByCallBack callBacks) {
        this.contactList = contactList;
        this.callBack = callBacks;
        this.context = context;
    }

    @NonNull
    @Override
    public AssignmentNotesSeenByAdapter.RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_home_my_assignments_recycler_view, parent, false);
        return new AssignmentNotesSeenByAdapter.RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AssignmentNotesSeenByAdapter.RecyclerViewHolder holder, int position) {
        holder.name.setText(contactList.get(position).getName());
        holder.description.setText(contactList.get(position).getPosition());

        storageReference.child("Users").child(String.valueOf(contactList.get(position).getId())).getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(context)
                    .load(uri.toString())
                    .centerCrop()
                    .into(holder.image);
        }).addOnFailureListener(e -> holder.image.setImageResource(R.drawable.unity));

        String dateTime = contactList.get(position).getSeen_date() + " " + contactList.get(position).getSeen_time();
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
}
