package com.unity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.unity.HomePageClient.CurrentActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class Message extends AppCompatActivity {
    RecyclerView recyclerView;
    MessageAdapter adapter;
    ProgressBar progressBar;
    FirestoreRecyclerOptions<MessageReadItems> options;
    EditText messageText;
    ImageButton send, back;
    CircularImageView photo;
    SharedPreferences sharedPreferences;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    TextView read, name_surname, last_seen;
    String dbID;
    ConstraintLayout info_layout;
    ImageView online_image;
    boolean zeroMessage = true, newDate = false, textChanged = false;
    ListenerRegistration unreadMessagesRegistration;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        sharedPreferences = getSharedPreferences("Log", MODE_PRIVATE);

        messageText = findViewById(R.id.messageText);
        send = findViewById(R.id.send);
        read = findViewById(R.id.read);
        recyclerView = findViewById(R.id.recycler_view);
        progressBar = findViewById(R.id.progressBar);
        back = findViewById(R.id.back);
        photo = findViewById(R.id.photo);
        name_surname = findViewById(R.id.name_surname);
        info_layout = findViewById(R.id.info_layout);
        last_seen = findViewById(R.id.last_seen);
        online_image = findViewById(R.id.online_image);

        String name = getIntent().getStringExtra("userName");
        name_surname.setText(name);

        if (name == null) {
            db.collection("Users").document(String.valueOf(getIntent().getIntExtra("userID", 0))).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    name_surname.setText(task.getResult().getString("name"));
                }
            });
        }

        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        storageReference.child("Users").child(String.valueOf(getIntent().getIntExtra("userID", 0))).getDownloadUrl()
                .addOnSuccessListener(uri -> Glide.with(this)
                        .load(uri.toString())
                        .centerCrop()
                        .into(photo));

        db.collection("Users").document(String.valueOf(sharedPreferences.getInt("id", 0))).collection("MessageRooms")
                .whereEqualTo("userID", getIntent().getIntExtra("userID", 0))
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
            try {
                dbID = queryDocumentSnapshots.getDocuments().get(0).getId();
                zeroMessage = false;
                setupRecyclerView();
                setSeen();
            } catch (IndexOutOfBoundsException ignored) {
                createOne();
                setSeen();
            }
        }).addOnCompleteListener(task -> {
            if (!zeroMessage) {
                db.collection("Users").document(String.valueOf(sharedPreferences.getInt("id", 0))).collection("MessageRooms")
                        .document(dbID).collection("Messages")
                        .orderBy("time_server", Query.Direction.DESCENDING).limit(1).get()
                        .addOnSuccessListener(queryDocumentSnapshots -> zeroMessage = queryDocumentSnapshots.isEmpty());
            }
        });

        send.setOnClickListener(view -> {
            send.setClickable(false);
            String message = messageText.getText().toString().trim();
            if (!message.equals("")) {
                String date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
                String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                String dateSent = date + " " + time;

                DocumentReference documentReference = db.collection("Users").document(String.valueOf(sharedPreferences.getInt("id", 0))).collection("MessageRooms").document(dbID);
                DocumentReference documentReference2 = db.collection("Users").document(String.valueOf(getIntent().getIntExtra("userID", 0))).collection("MessageRooms").document(dbID);

                CollectionReference messagesToReadTheirs = db.collection("Users").document(String.valueOf(getIntent().getIntExtra("userID", 0))).collection("MessagesToRead");

                CollectionReference collectionReferenceMessageOurs = db.collection("Users").document(String.valueOf(sharedPreferences.getInt("id", 0))).collection("MessageRooms").document(dbID).collection("Messages");
                CollectionReference collectionReferenceMessageTheirs = db.collection("Users").document(String.valueOf(getIntent().getIntExtra("userID", 0))).collection("MessageRooms").document(dbID).collection("Messages");

                if (zeroMessage) {
                    newDate = true;
                }

                MessageItems messageItems = new MessageItems(message, sharedPreferences.getInt("id", 0), getIntent().getIntExtra("userID", 0),
                        false, false, false, dateSent, "", FieldValue.serverTimestamp(), newDate, "active", "", false);

                collectionReferenceMessageOurs.add(messageItems).addOnSuccessListener(documentReferenceForMessageSent -> collectionReferenceMessageOurs.document(documentReferenceForMessageSent.getId()).update("messageID", documentReferenceForMessageSent.getId())).addOnCompleteListener(task -> {
                    collectionReferenceMessageTheirs.document(task.getResult().getId()).set(messageItems);

                    collectionReferenceMessageTheirs.document(task.getResult().getId()).update("messageID", task.getResult().getId()).addOnSuccessListener(aVoid ->
                            messagesToReadTheirs.document(task.getResult().getId()).set(messageItems));

                    documentReference.update("last_messageID", task.getResult().getId());
                    documentReference2.update("last_messageID", task.getResult().getId());
                });

                zeroMessage = false;
                sendNotifications(message);
                messageText.setText("");
                send.setClickable(true);

                collectionReferenceMessageOurs.orderBy("time_server", Query.Direction.DESCENDING).limit(1).get().addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String timeSent = Objects.requireNonNull(documentSnapshot.getString("time_sent")).split(" ")[0];
                        newDate = !timeSent.equals(date);
                    }
                }).addOnCompleteListener(task -> {
                    documentReference.update("last_message", message);
                    documentReference2.update("last_message", message);

                    documentReference.update("time_server", FieldValue.serverTimestamp());
                    documentReference2.update("time_server", FieldValue.serverTimestamp());

                    documentReference.update("senderID", sharedPreferences.getInt("id", 0));
                    documentReference2.update("senderID", sharedPreferences.getInt("id", 0));

                    documentReference.update("time", dateSent);
                    documentReference2.update("time", dateSent);

                    documentReference.update("situation", "active");
                    documentReference2.update("situation", "active");

                    documentReference.update("read_by_me", true);
                    documentReference2.update("read_by_me", false);

                    documentReference.update("show", true);
                    documentReference2.update("show", true);

                    documentReference.update("read", false);
                    documentReference.update("time_read", "");
                });
            } else {
                send.setClickable(true);
            }
        });

        back.setOnClickListener(view -> finish());
        photo.setOnClickListener(view -> openUserProfile());
        info_layout.setOnClickListener(view -> openUserProfile());

        db.collection("Users").document(String.valueOf(getIntent().getIntExtra("userID", 0)))
                .addSnapshotListener((value, error) -> {
                    if (value != null && value.getString("online") != null) {
                        String online = value.getString("online");
                        if (online.equalsIgnoreCase("online") && value.getString("message_situation").equalsIgnoreCase("typing")) {
                            last_seen.setText("Yazıyor...");
                            online_image.setVisibility(View.VISIBLE);
                        } else if (online.equalsIgnoreCase("online")) {
                            last_seen.setText("Çevrimiçi");
                            online_image.setVisibility(View.VISIBLE);
                        } else if (online != null) {
                            online_image.setVisibility(View.GONE);
                            DateFormatterForMessage dateFormatter = new DateFormatterForMessage();
                            String convTime = dateFormatter.format(online);
                            last_seen.setText(convTime);
                        }


                    }
                });

        messageText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (messageText.getText().toString().trim().length() > 0 && !textChanged) {
                    db.collection("Users").document(String.valueOf(sharedPreferences.getInt("id", 0))).update("message_situation", "typing");
                    textChanged = true;
                } else if (messageText.getText().toString().trim().length() <= 0) {
                    db.collection("Users").document(String.valueOf(sharedPreferences.getInt("id", 0))).update("message_situation", "");
                    textChanged = false;
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        CurrentActivity.setCurrectActivity("FragmentContact");
    }

    private void setSeen() {
        if (!zeroMessage) {
            String dateRead = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()) + " " + new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

            DocumentReference documentReference = db.collection("Users").document(String.valueOf(getIntent().getIntExtra("userID", 0))).collection("MessageRooms").document(dbID);
            DocumentReference documentReference2 = db.collection("Users").document(String.valueOf(sharedPreferences.getInt("id", 0))).collection("MessageRooms").document(dbID);

            documentReference.get().addOnSuccessListener(documentSnapshot -> {
                if (!Objects.requireNonNull(documentSnapshot.getBoolean("read"))) {
                    documentReference2.update("read_by_me", true);
                    documentReference.update("read", true);
                    documentReference.update("time_read", dateRead);
                }
            });

            CollectionReference collectionReferenceMessageTheirs = db.collection("Users").document(String.valueOf(getIntent().getIntExtra("userID", 0))).collection("MessageRooms").document(dbID).collection("Messages");

            collectionReferenceMessageTheirs.orderBy("time_server", Query.Direction.DESCENDING).limit(1).get().addOnSuccessListener(queryDocumentSnapshots -> {
                for (DocumentSnapshot documentSnapshots : queryDocumentSnapshots) {
                    if (Objects.requireNonNull(documentSnapshots.getLong("senderID")).intValue() != sharedPreferences.getInt("id", 0)) {
                        collectionReferenceMessageTheirs.document(documentSnapshots.getId()).update("read", true);
                        collectionReferenceMessageTheirs.document(documentSnapshots.getId()).update("time_read", dateRead);
                        deleteMessagesToRead(documentSnapshots.getId());
                    }
                }
            });

            CollectionReference messagesToReadTheirs = db.collection("Users").document(String.valueOf(sharedPreferences.getInt("id", 0))).collection("MessagesToRead");
            Query query = messagesToReadTheirs.whereEqualTo("senderID", getIntent().getIntExtra("userID", 0));
            unreadMessagesRegistration = query.addSnapshotListener((value, error) -> {
                if (value != null){
                    for (DocumentSnapshot documentSnapshot : value.getDocuments()){
                        messagesToReadTheirs.document(documentSnapshot.getId()).delete();
                    }
                }
            });
        }
    }

    private void deleteMessagesToRead(String id) {
        CollectionReference messagesToReadTheirs = db.collection("Users").document(String.valueOf(getIntent().getIntExtra("userID", 0))).collection("MessagesToRead");

        messagesToReadTheirs.document(String.valueOf(id)).delete();
    }

    private void createOne() {
        String dateCreated = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()) + " " + new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

            db.collection("Users").document(String.valueOf(sharedPreferences.getInt("id", 0))).collection("MessageRooms")
                    .add(new MessageRoomsUserCreateItems(getIntent().getStringExtra("userName"), "", dateCreated, "", dbID, getIntent().getIntExtra("userID", 0), 0,
                            false, true, "passive", "", FieldValue.serverTimestamp()))
            .addOnCompleteListener(task -> {
                dbID = task.getResult().getId();

                db.collection("Users").document(String.valueOf(getIntent().getIntExtra("userID", 0))).collection("MessageRooms")
                        .document(dbID).set(new MessageRoomsUserCreateItems(sharedPreferences.getString("name", ""), "", dateCreated, "", dbID, sharedPreferences.getInt("id", 0), 0,
                        false, false, "passive", "", FieldValue.serverTimestamp()));

                setupRecyclerView();
            });
    }

    private void sendNotifications(String messageReceived) {
        NotificationsMessage notifications = new NotificationsMessage();
        notifications.sendNotification(String.valueOf(getIntent().getIntExtra("userID", 0)), this,
                sharedPreferences.getString("name", "Unity Mobil"), messageReceived, String.valueOf(sharedPreferences.getInt("id", 0)));
    }

    private void setupRecyclerView() {
        CollectionReference messageRoomReference = db.collection("Users").document(String.valueOf(sharedPreferences.getInt("id", 0))).collection("MessageRooms")
                .document(dbID).collection("Messages");

        Query query = messageRoomReference.orderBy("time_server");
        options = new FirestoreRecyclerOptions.Builder<MessageReadItems>()
                .setQuery(query, MessageReadItems.class)
                .build();

        adapter = new MessageAdapter(options, this);
        adapter.startListening();
        recyclerView.setHasFixedSize(true);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setItemViewCacheSize(500);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                recyclerView.scrollToPosition(adapter.getItemCount() - 1);

                String dateRead = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()) + " " + new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                DocumentReference documentReference = db.collection("Users").document(String.valueOf(sharedPreferences.getInt("id", 0))).collection("MessageRooms").document(dbID);
                documentReference.update("read_by_me", true);

                CollectionReference messageRoomReferenceTheirs = db.collection("Users").document(String.valueOf(getIntent().getIntExtra("userID", 0))).collection("MessageRooms")
                        .document(dbID).collection("Messages");

                messageRoomReferenceTheirs.whereEqualTo("read", false).get().addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        if (Objects.requireNonNull(documentSnapshot.getLong("senderID")).intValue() != sharedPreferences.getInt("id", 0)) {
                            messageRoomReferenceTheirs.document(documentSnapshot.getId()).update("read", true);
                            messageRoomReferenceTheirs.document(documentSnapshot.getId()).update("time_read", dateRead);
                        }
                    }
                });

                DocumentReference documentReferenceTheirs = db.collection("Users").document(String.valueOf(getIntent().getIntExtra("userID", 0))).collection("MessageRooms").document(dbID);
                documentReferenceTheirs.get().addOnSuccessListener(documentSnapshot -> {
                    documentReferenceTheirs.update("read", true);
                    documentReferenceTheirs.update("time_read", dateRead);
                });
            }
        });

        adapter.setOnItemClickListener(new MessageAdapter.OnItemClickListener() {
            @Override
            public void onUserImageClicked() {
                openUserProfile();
            }

            @Override
            public void onItemLongClick(String messageID, int senderID, int receiverID, String situation) {
                if (situation.equalsIgnoreCase("deleted")) {
                    return;
                }
                if (senderID == sharedPreferences.getInt("id", 0)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Message.this);
                    builder.setTitle(R.string.choose_what_to_do)
                            .setItems(R.array.message_alert_dialog, (dialog, which) -> {
                                if (which == 0) {
                                    AlertDialog.Builder builder2 = new AlertDialog.Builder(Message.this);
                                    builder2.setMessage(R.string.are_you_sure_to_delete_message_from_yourself)
                                            .setPositiveButton(R.string.yes, (dialogInterface, i) -> deleteFromMe(messageID))
                                            .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {

                                            });
                                    builder2.create().show();
                                } else if (which == 1) {
                                    AlertDialog.Builder builder2 = new AlertDialog.Builder(Message.this);
                                    builder2.setMessage(R.string.are_you_sure_to_delete_message_from_everybody)
                                            .setPositiveButton(R.string.yes, (dialogInterface, i) -> deleteFromEverybody(messageID, receiverID))
                                            .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {

                                            });
                                    builder2.create().show();
                                } else {
                                    dialog.dismiss();
                                }
                            });
                    builder.create().show();
                } else if (receiverID == sharedPreferences.getInt("id", 0)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Message.this);
                    builder.setTitle(R.string.choose_what_to_do)
                            .setItems(R.array.message_alert_dialog_for_receiver, (dialog, which) -> {
                                if (which == 0) {
                                    AlertDialog.Builder builder2 = new AlertDialog.Builder(Message.this);
                                    builder2.setMessage(R.string.are_you_sure_to_delete_message_from_yourself)
                                            .setPositiveButton(R.string.yes, (dialogInterface, i) -> deleteFromMe(messageID))
                                            .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {

                                            });
                                    builder2.create().show();
                                } else {
                                    dialog.dismiss();
                                }
                            });
                    builder.create().show();
                }
            }
        });

        recyclerView.setAdapter(adapter);
        progressBar.setVisibility(View.GONE);
    }

    private void deleteFromEverybody(String messageID, int userID) {
        DocumentReference documentReferenceForMessageMine = db.collection("Users").document(String.valueOf(sharedPreferences.getInt("id", 0)))
                .collection("MessageRooms").document(dbID).collection("Messages").document(messageID);

        DocumentReference documentReferenceForMessageTheirs = db.collection("Users").document(String.valueOf(userID))
                .collection("MessageRooms").document(dbID).collection("Messages").document(messageID);

        documentReferenceForMessageMine.update("situation", "deleted");
        documentReferenceForMessageTheirs.update("situation", "deleted");

        DocumentReference docRef = db.collection("Users").document(String.valueOf(userID))
                .collection("MessageRooms").document(dbID);

        DocumentReference docRefOurs = db.collection("Users").document(String.valueOf(sharedPreferences.getInt("id", 0)))
                .collection("MessageRooms").document(dbID);

        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (Objects.requireNonNull(documentSnapshot.getString("last_messageID")).equalsIgnoreCase(messageID)){
                docRef.update("last_message", "Bu mesaj silindi");
                docRefOurs.update("last_message", "Bu mesaj silindi");
            }
        });
    }

    private void deleteFromMe(String messageID) {
        DocumentReference documentReferenceForMessageMine = db.collection("Users").document(String.valueOf(sharedPreferences.getInt("id", 0)))
                .collection("MessageRooms").document(dbID).collection("Messages").document(messageID);

        documentReferenceForMessageMine.delete();
    }

    private void openUserProfile() {
        Intent intent = new Intent(Message.this, WorkersInformation.class);
        intent.putExtra("name", getIntent().getStringExtra("userName"));
        intent.putExtra("id", getIntent().getIntExtra("userID", 0));
        intent.putExtra("position", getIntent().getStringExtra("position"));
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            adapter.startListening();
        } catch (Exception ignored) { }
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            adapter.stopListening();
        } catch (Exception ignored) { }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try{
            unreadMessagesRegistration.remove();
        }
        catch (Exception ignored) { }
    }
}