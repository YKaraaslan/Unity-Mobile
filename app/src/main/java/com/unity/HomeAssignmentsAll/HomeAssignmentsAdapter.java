package com.unity.HomeAssignmentsAll;

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
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.unity.AssignmentsCreateItems;
import com.unity.DateFormatter;
import com.unity.R;

public class HomeAssignmentsAdapter extends FirestoreRecyclerAdapter<AssignmentsCreateItems, HomeAssignmentsAdapter.RecyclerViewHolder> {
    Context context;
    StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    private HomeAssignmentsAdapter.OnItemClickListener listener;

    public HomeAssignmentsAdapter(@NonNull FirestoreRecyclerOptions<AssignmentsCreateItems> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull HomeAssignmentsAdapter.RecyclerViewHolder holder, int position, @NonNull AssignmentsCreateItems model) {
        holder.description.setText(model.getNote());

        storageReference.child("Users").child(String.valueOf(model.getInChargeID())).getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(context)
                    .load(uri.toString())
                    .centerCrop()
                    .into(holder.image);
        }).addOnFailureListener(e -> holder.image.setImageResource(R.drawable.unity));

        String lastTitle = "(" + model.getId() + ") - " + model.getTitle();
        if (lastTitle.length() > 35){
            lastTitle = lastTitle.substring(0, 35) + "...";
        }
        holder.name.setText(lastTitle);

        String dateTime = model.getDate() + " " + model.getTime();
        DateFormatter dateFormatter = new DateFormatter();
        String convTime = dateFormatter.format(dateTime);
        holder.time.setText(convTime);
    }

    @NonNull
    @Override
    public HomeAssignmentsAdapter.RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_home_my_assignments_active_recycler_view, parent, false);
        return new HomeAssignmentsAdapter.RecyclerViewHolder(view);
    }

    public void setOnItemClickListener(HomeAssignmentsAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot documentSnapshot);
        void onItemLongClick(DocumentSnapshot documentSnapshot);
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
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(getSnapshots().getSnapshot(position));
                }
            });

            cardView.setOnLongClickListener(view -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemLongClick(getSnapshots().getSnapshot(position));
                }
                return false;
            });
        }
    }
}
