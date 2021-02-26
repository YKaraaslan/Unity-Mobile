package com.unity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static android.content.Context.MODE_PRIVATE;

public class MessageArchiveAdapter  extends FirestoreRecyclerAdapter<FragmentContactItem, MessageArchiveAdapter.RecyclerViewHolder> {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private MessageArchiveAdapter.OnItemClickListener listener;
    Context context;
    SharedPreferences sharedPreferences;
    StorageReference storageReference = FirebaseStorage.getInstance().getReference();

    public MessageArchiveAdapter(@NonNull FirestoreRecyclerOptions<FragmentContactItem> options, Context context) {
        super(options);
        this.context = context;
        sharedPreferences = context.getSharedPreferences("Log", MODE_PRIVATE);
    }

    @NonNull
    @Override
    public MessageArchiveAdapter.RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_contact_recyclerview, parent, false);
        return new MessageArchiveAdapter.RecyclerViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onBindViewHolder(@NonNull MessageArchiveAdapter.RecyclerViewHolder holder, int position, @NonNull FragmentContactItem model) {
        holder.name.setText(model.getUser_name());
        String lastMessage = model.getLast_message();
        if (lastMessage.length() > 40){
            lastMessage = lastMessage.substring(0, 40) + "...";
        }
        holder.time.setText(format(model.getTime()));

        if (model.isRead() && model.getSenderID() == sharedPreferences.getInt("id", 0)){
            holder.seen.setImageResource(R.drawable.ic_seen);
        }
        else if (model.getSenderID() != sharedPreferences.getInt("id", 0)){
            holder.seen.setVisibility(View.GONE);
        }
        else {
            holder.seen.setImageResource(R.drawable.ic_check);
        }

        holder.unread_messages.setText(" Arşiv " );
        holder.unread_messages.setVisibility(View.VISIBLE);

        storageReference.child("Users").child(String.valueOf(model.getUserID())).getDownloadUrl().addOnSuccessListener(uri -> Glide.with(context)
                .load(uri.toString())
                .centerCrop()
                .into(holder.image)).addOnFailureListener(e -> holder.image.setImageResource(R.drawable.unity));

        String finalLastMessage = lastMessage;
        db.collection("Users").document(String.valueOf(model.getSenderID()))
                .addSnapshotListener((value, error) -> {
                    if (value != null && value.getString("online") != null){
                        String online = value.getString("online");
                        if (online.equalsIgnoreCase("online") && value.getString("message_situation").equalsIgnoreCase("typing")){
                            holder.description.setText("Yazıyor...");
                            holder.description.setTypeface(null, Typeface.BOLD);
                            holder.description.setTextColor(Color.rgb(23, 110, 186));
                        }
                        else {
                            holder.description.setText(finalLastMessage);
                            holder.description.setTypeface(null, Typeface.NORMAL);
                            holder.description.setTextColor(Color.GRAY);
                        }
                    }
                });
    }

    public void setOnItemClickListener(MessageArchiveAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot documentSnapshot);
        void onItemLongClick(DocumentSnapshot documentSnapshot);
    }

    public class RecyclerViewHolder extends RecyclerView.ViewHolder {
        CircularImageView image;
        ImageView seen;
        TextView name, description, time, unread_messages;
        CardView cardView;
        @SuppressLint("ResourceAsColor")
        public RecyclerViewHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.contact_image);
            name = itemView.findViewById(R.id.contact_name);
            description = itemView.findViewById(R.id.contact_description);
            time = itemView.findViewById(R.id.contact_time);
            seen = itemView.findViewById(R.id.seen);
            cardView = itemView.findViewById(R.id.contact_card);
            unread_messages = itemView.findViewById(R.id.unread_messages);

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
                return true;
            });
        }
    }

    public String format(String date) {
        String convTime = null;
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        try {
            Date past = inputFormat.parse(date);
            Date now = new Date();
            long hours = TimeUnit.MILLISECONDS.toHours(now.getTime() - Objects.requireNonNull(past).getTime());

            String[] timeReceived = date.split(" ")[1].split(":");
            String dateShow = date.split(" ")[0];
            String time = timeReceived[0] + ":" + timeReceived[1];

            if (hours < 12) {
                convTime = time;
            } else {
                convTime = dateShow;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return convTime;
    }
}
