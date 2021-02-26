package com.unity.HomePageClient;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.mikhaellopez.circularimageview.CircularImageView;
import com.unity.AssignmentsUserDBItems;
import com.unity.DateFormatter;
import com.unity.R;

public class HomeMyAssignmentsCompletedAdapter extends FirestoreRecyclerAdapter<AssignmentsUserDBItems, HomeMyAssignmentsCompletedAdapter.RecyclerViewHolder> {
    Context context;
    StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    private HomeMyAssignmentsCompletedAdapter.OnItemClickListener listener;

    public HomeMyAssignmentsCompletedAdapter(@NonNull FirestoreRecyclerOptions<AssignmentsUserDBItems> options, Context context) {
        super(options);
        this.context = context;
    }

    @NonNull
    @Override
    public HomeMyAssignmentsCompletedAdapter.RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_home_my_assignments_completed_recycler_view, parent, false);
        return new HomeMyAssignmentsCompletedAdapter.RecyclerViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position, @NonNull AssignmentsUserDBItems model) {
        String lastTitle = "(" + model.getAssignmentID() + ") - " + model.getTitle();
        if (lastTitle.length() > 35){
            lastTitle = lastTitle.substring(0, 35) + "...";
        }
        holder.name.setText(lastTitle);

        holder.description.setText(model.getNote());

        try{
            storageReference.child("Users").child(String.valueOf(model.getInChargeID())).getDownloadUrl().addOnSuccessListener(uri -> Glide.with(context)
                    .load(uri.toString())
                    .centerCrop()
                    .into(holder.image)).addOnFailureListener(e -> holder.image.setImageResource(R.drawable.unity));
        }
        catch (Exception ignored) { }

        String dateTime = model.getFinish_date() + " " + model.getFinish_time();
        DateFormatter dateFormatter = new DateFormatter();
        String convTime = dateFormatter.format(dateTime);
        holder.time.setText(convTime);
    }


    public void setOnItemClickListener(HomeMyAssignmentsCompletedAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot documentSnapshot);
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

            cardView.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(getSnapshots().getSnapshot(position));
                }
            });
        }
    }
}
