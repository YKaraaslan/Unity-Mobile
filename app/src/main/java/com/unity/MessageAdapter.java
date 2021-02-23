package com.unity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;

import static android.content.Context.MODE_PRIVATE;

public class MessageAdapter extends FirestoreRecyclerAdapter<MessageReadItems, RecyclerView.ViewHolder> {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private MessageAdapter.OnItemClickListener listener;
    Context context;
    SharedPreferences sharedPreferences;
    Uri photoUri;

    public MessageAdapter(@NonNull FirestoreRecyclerOptions<MessageReadItems> options, Context context) {
        super(options);
        this.context = context;
        sharedPreferences = context.getSharedPreferences("Log", MODE_PRIVATE);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == 0){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_receive_item, parent, false);
            return new ViewHolderReceiver(view);
        }
        else{
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_send_item, parent, false);
            return new ViewHolderSender(view);
        }
    }

    @Override
    protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull MessageReadItems model) {
        if (getItem(position).getSenderID() == sharedPreferences.getInt("id", 0)) {
            ((ViewHolderSender) holder).bind(position);
        } else {
            ((ViewHolderReceiver) holder).bind(position);
        }
    }

    public void setOnItemClickListener(MessageAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onUserImageClicked();
        void onItemLongClick(String id, int senderID, int receiverID, String situation);
    }

    private class ViewHolderReceiver extends RecyclerView.ViewHolder {
        TextView text, time, datetime;
        LinearLayout linear;
        CircularImageView user_photo;
        ConstraintLayout text_layout;

        public ViewHolderReceiver(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.text);
            time = itemView.findViewById(R.id.time);
            datetime = itemView.findViewById(R.id.datetime);
            linear = itemView.findViewById(R.id.linear);
            user_photo = itemView.findViewById(R.id.user_photo);
            text_layout = itemView.findViewById(R.id.text_layout);
        }

        void bind(int position) {
            if (getItem(position).getSituation().equalsIgnoreCase("deleted")){
                text.setText("Bu mesaj silindi");
                text.setTypeface(null, Typeface.ITALIC);
                text.setTextColor(Color.GRAY);
                time.setVisibility(View.GONE);
            }
            else{
                text.setText(getItem(position).getMessage());
                text.setTypeface(null, Typeface.NORMAL);
                text.setTextColor(Color.BLACK);
                time.setVisibility(View.VISIBLE);
            }

            String[] timeReceived = getItem(position).getTime_sent().split(" ")[1].split(":");
            String lastTime = timeReceived[0] + ":" + timeReceived[1];
            time.setText(lastTime);

            if (getItem(position).isNew_date()){
                datetime.setVisibility(View.VISIBLE);
                datetime.setText(getItem(position).getTime_sent().split(" ")[0]);
            }
            else {
                datetime.setVisibility(View.GONE);
            }

            if (photoUri == null){
                StorageReference storageReference = FirebaseStorage.getInstance().getReference();
                storageReference.child("Users").child(String.valueOf(getItem(position).getSenderID())).getDownloadUrl().addOnSuccessListener(uri -> {
                    try{
                        Glide.with(context)
                                .load(uri.toString())
                                .centerCrop()
                                .into(user_photo);
                        photoUri = uri;
                    }
                    catch (Exception ignored) { }
                }).addOnFailureListener(e -> user_photo.setImageResource(R.drawable.unity));
            }
            else {
                Glide.with(context)
                        .load(photoUri.toString())
                        .centerCrop()
                        .into(user_photo);
            }

            user_photo.setOnClickListener(view -> {
                listener.onUserImageClicked();
            });

            text_layout.setOnLongClickListener(view -> {
                listener.onItemLongClick(getItem(position).getMessageID(), getItem(position).getSenderID(), getItem(position).getReceiverID(), getItem(position).getSituation());
                return true;
            });
        }
    }

    private class ViewHolderSender extends RecyclerView.ViewHolder {
        TextView text, time, datetime;
        ImageView read;
        LinearLayout linear;
        ConstraintLayout text_layout;

        public ViewHolderSender(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.text);
            time = itemView.findViewById(R.id.time);
            read = itemView.findViewById(R.id.read);
            datetime = itemView.findViewById(R.id.datetime);
            linear = itemView.findViewById(R.id.linear);
            text_layout = itemView.findViewById(R.id.text_layout);
        }

        void bind(int position) {
            if (getItem(position).getSituation().equalsIgnoreCase("deleted")){
                text.setText("Bu mesaj silindi");
                text.setTypeface(null, Typeface.ITALIC);
                text.setTextColor(Color.GRAY);
                time.setVisibility(View.GONE);
                read.setVisibility(View.GONE);
            }
            else{
                text.setText(getItem(position).getMessage());
                text.setTypeface(null, Typeface.NORMAL);
                text.setTextColor(Color.BLACK);
                time.setVisibility(View.VISIBLE);
            }

            if (getItem(position).getSenderID() == sharedPreferences.getInt("id", 0) && getItem(position).isRead()){
                read.setImageResource(R.drawable.ic_seen);
            }
            else if (getItem(position).getSenderID() == sharedPreferences.getInt("id", 0) && getItem(position).isReceived()){
                read.setImageResource(R.drawable.ic_received);
            }

            String[] timeReceived = getItem(position).getTime_sent().split(" ")[1].split(":");
            String lastTime = timeReceived[0] + ":" + timeReceived[1];
            time.setText(lastTime);

            if (getItem(position).isNew_date()){
                datetime.setVisibility(View.VISIBLE);
                datetime.setText(getItem(position).getTime_sent().split(" ")[0]);
            }
            else {
                datetime.setVisibility(View.GONE);
            }

            text_layout.setOnLongClickListener(view -> {
                listener.onItemLongClick(getItem(position).getMessageID(), getItem(position).getSenderID(), getItem(position).getReceiverID(), getItem(position).getSituation());
                return true;
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(getItem(position).getSenderID() == sharedPreferences.getInt("id", 0))
            return 1;
        else
            return 0;
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }
}
