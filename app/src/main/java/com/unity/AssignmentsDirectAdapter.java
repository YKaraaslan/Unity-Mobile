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
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.List;

public class AssignmentsDirectAdapter extends RecyclerView.Adapter<AssignmentsDirectAdapter.RecyclerViewHolder> {
    List<AssignmentsDirectItems> contactList;
    AssignmentsDirectCallBack callBack;
    Context context;
    StorageReference storageReference = FirebaseStorage.getInstance().getReference();

    public AssignmentsDirectAdapter(Context context, List<AssignmentsDirectItems> contactList, AssignmentsDirectCallBack callBacks) {
        this.contactList = contactList;
        this.callBack = callBacks;
        this.context = context;
    }

    @NonNull
    @Override
    public AssignmentsDirectAdapter.RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_assignments_direct_recycler_view, parent, false);
        return new AssignmentsDirectAdapter.RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AssignmentsDirectAdapter.RecyclerViewHolder holder, int position) {
        holder.name.setText(contactList.get(position).getName());
        if (contactList.get(position).isSeen_situation()){
            holder.seen_situation.setImageResource(R.drawable.ic_seen);

            storageReference.child("Users").child(String.valueOf(contactList.get(position).getId())).getDownloadUrl().addOnSuccessListener(uri -> {
                Glide.with(context)
                        .load(uri.toString())
                        .centerCrop()
                        .into(holder.image);
            }).addOnFailureListener(e -> holder.image.setImageResource(R.drawable.unity));

            String dateTime = contactList.get(position).getSeen_date() + " " + contactList.get(position).getSeen_time();
            DateFormatter dateFormatter = new DateFormatter();
            String convTime = dateFormatter.format(dateTime);
            holder.seen_time.setText(convTime);
        }
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    public class RecyclerViewHolder extends RecyclerView.ViewHolder {
        CircularImageView image;
        ImageView seen_situation;
        TextView name, seen_time;
        CardView cardView;
        public RecyclerViewHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.workers_image);
            name = itemView.findViewById(R.id.workers_name);
            cardView = itemView.findViewById(R.id.workers_card);
            seen_time = itemView.findViewById(R.id.seen_time);
            seen_situation = itemView.findViewById(R.id.seen_situation);

            cardView.setOnClickListener(view -> {
                callBack.onItemClick(getAdapterPosition());
            });

            cardView.setOnLongClickListener(view -> {
                try{
                    callBack.onItemLongClick(getAdapterPosition(), contactList.get(getAdapterPosition()).assignedID);
                }
                catch (Exception ignored) { }
                return false;
            });
        }
    }
}
