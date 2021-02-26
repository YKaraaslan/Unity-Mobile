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
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


public class WorkersInformationAssignmentsAdapter extends FirestoreRecyclerAdapter<AssignmentsUserDBItems, WorkersInformationAssignmentsAdapter.RecyclerViewHolder> {
    Context context;
    StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    private WorkersInformationAssignmentsAdapter.OnItemClickListener listener;

    public WorkersInformationAssignmentsAdapter(@NonNull FirestoreRecyclerOptions<AssignmentsUserDBItems> options, Context context) {
        super(options);
        this.context = context;
    }

    @NonNull
    @Override
    public WorkersInformationAssignmentsAdapter.RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_home_my_assignments_active_recycler_view, parent, false);
        return new WorkersInformationAssignmentsAdapter.RecyclerViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position, @NonNull AssignmentsUserDBItems model) {
        holder.description.setText(model.getNote());

        storageReference.child("Users").child(String.valueOf(model.getInChargeID())).getDownloadUrl().addOnSuccessListener(uri -> Glide.with(context)
                .load(uri.toString())
                .centerCrop()
                .into(holder.image));

        String lastTitle = "(" + model.getAssignmentID() + ") - " + model.getTitle();
        if (lastTitle.length() > 30){
            lastTitle = lastTitle.substring(0, 30) + "...";
        }
        holder.name.setText(lastTitle);

        String dateTime = model.getDate() + " " + model.getTime();
        DateFormatter dateFormatter = new DateFormatter();
        String convTime = dateFormatter.format(dateTime);
        holder.time.setText(convTime);
    }

    public void setOnItemClickListener(WorkersInformationAssignmentsAdapter.OnItemClickListener listener) {
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
