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

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;


public class ReminderAdapter extends FirestoreRecyclerAdapter<ReminderItem, ReminderAdapter.RecyclerViewHolder> {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private OnItemClickListener listener;
    StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    Context context;

    public ReminderAdapter(@NonNull FirestoreRecyclerOptions<ReminderItem> options, Context context) {
        super(options);
        this.context = context;
    }

    @NonNull
    @Override
    public ReminderAdapter.RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_workers_recyclerview, parent, false);
        return new ReminderAdapter.RecyclerViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position, @NonNull ReminderItem model) {
        holder.cardView.setAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_transition_animation));

        holder.name.setText(model.getTitle());
        holder.description.setText(model.getNote());
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(int id, String title, String note, String date_created, String time_created, String date_end, String time_end);
        void onItemLongClick(int id, String title, String note, String date_created, String time_created, String date_end, String time_end);
    }

    public class RecyclerViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name, description, time;
        CardView cardView;
        public RecyclerViewHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.workers_image);
            name = itemView.findViewById(R.id.workers_name);
            description = itemView.findViewById(R.id.workers_description);
            time = itemView.findViewById(R.id.workers_time);
            cardView = itemView.findViewById(R.id.workers_card);

            cardView.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(Objects.requireNonNull(getSnapshots().getSnapshot(position).getLong("id")).intValue(), getSnapshots().getSnapshot(position).getString("title"),
                            getSnapshots().getSnapshot(position).getString("note"),getSnapshots().getSnapshot(position).getString("date_created"),
                            getSnapshots().getSnapshot(position).getString("time_created"), getSnapshots().getSnapshot(position).getString("date_end"),
                            getSnapshots().getSnapshot(position).getString("time_end"));
                }
            });

            cardView.setOnLongClickListener(view -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemLongClick(getItem(position).getId(),getItem(position).getTitle(), getItem(position).getNote(), getItem(position).getDate_created(),
                            getItem(position).getTime_created(), getItem(position).getDate_end(), getItem(position).getTime_end());
                }
                return false;
            });
        }
    }
}
