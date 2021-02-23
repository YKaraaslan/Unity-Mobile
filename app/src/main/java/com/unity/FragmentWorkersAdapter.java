package com.unity;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;

public class FragmentWorkersAdapter extends FirestoreRecyclerAdapter<FragmentWorkersItem, FragmentWorkersAdapter.RecyclerViewHolder> {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private OnItemClickListener listener;
    StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    Context context;

    public FragmentWorkersAdapter(@NonNull FirestoreRecyclerOptions<FragmentWorkersItem> options, Context context) {
        super(options);
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_workers_recyclerview, parent, false);
        return new RecyclerViewHolder(view);
    }


    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot documentSnapshot);
    }

    @Override
    protected void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position, @NonNull FragmentWorkersItem model) {
        holder.name.setText(model.getName());
        holder.description.setText(model.getPosition());

        storageReference.child("Users").child(String.valueOf(model.getId())).getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(context)
                    .load(uri.toString())
                    .centerCrop()
                    .into(holder.image);
        }).addOnFailureListener(e -> holder.image.setImageResource(R.drawable.unity));

        String dateTime = model.getOnline();
        try{
            if (!dateTime.equalsIgnoreCase("online")){
                DateFormatter dateFormatter = new DateFormatter();
                String convTime = dateFormatter.format(dateTime);
                holder.time.setText(convTime);
                holder.time.setVisibility(View.VISIBLE);
                holder.online.setVisibility(View.GONE);
            }
            else{
                holder.online.setImageResource(R.drawable.online);
                holder.online.setVisibility(View.VISIBLE);
                holder.time.setVisibility(View.GONE);
            }
        }
        catch (Exception ignored) { }
    }


    public class RecyclerViewHolder extends RecyclerView.ViewHolder {
        CircularImageView image;
        ImageView imageContainer, online;
        TextView name, description, time;
        CardView cardView;
        public RecyclerViewHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.workers_image);
            imageContainer = itemView.findViewById(R.id.workersImage);
            name = itemView.findViewById(R.id.workers_name);
            description = itemView.findViewById(R.id.workers_description);
            time = itemView.findViewById(R.id.workers_time);
            cardView = itemView.findViewById(R.id.workers_card);
            online = itemView.findViewById(R.id.online);

            cardView.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(getSnapshots().getSnapshot(position));
                }
            });
        }
    }
}
