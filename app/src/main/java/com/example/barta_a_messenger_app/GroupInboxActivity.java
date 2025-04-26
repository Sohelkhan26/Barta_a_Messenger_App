package com.example.barta_a_messenger_app;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.widget.AppCompatImageView;
import de.hdodenhof.circleimageview.CircleImageView;

public class GroupInboxActivity extends AppCompatActivity {

    private static final String TAG = "GroupInboxActivity";
    private TextView groupNameTextView;
    private RecyclerView chatRecyclerView;
    private EditText inputMessage;
    private AppCompatImageView sendButton;
    private ImageButton imageSendButton;
    private AppCompatImageView backButton;
    private AppCompatImageView infoButton;
    private CircleImageView groupImageView;

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference groupRef;

    private String groupId;
    private String currentUserId;
    private ArrayList<MessageModel> messageList;
    private ChatAdapter chatAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_inbox);

        // Log intent data
        Log.d(TAG, "onCreate: Starting GroupInboxActivity");
        Log.d(TAG, "onCreate: Intent extras: " + getIntent().getExtras());

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        // Get group ID from intent
        groupId = getIntent().getStringExtra("groupId");
        String groupName = getIntent().getStringExtra("groupName");

        // Log group data
        Log.d(TAG, "onCreate: Group ID: " + groupId);
        Log.d(TAG, "onCreate: Group Name: " + groupName);

        // Initialize views
        groupNameTextView = findViewById(R.id.groupName);
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        inputMessage = findViewById(R.id.inputMessage);
        sendButton = findViewById(R.id.send);
        imageSendButton = findViewById(R.id.image_send_button);
        backButton = findViewById(R.id.imageBack);
        infoButton = findViewById(R.id.imageInfo);
        groupImageView = findViewById(R.id.groupImageView);

        // Set group name
        groupNameTextView.setText(groupName);

        // Setup RecyclerView
        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList, this);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        // Load messages
        loadMessages();

        // Send message button click
        sendButton.setOnClickListener(v -> {
            sendMessage();
        });

        // Image send button click (you can implement this later)
        imageSendButton.setOnClickListener(v -> {
            // Implement image sending functionality
        });
    }

    private void loadMessages() {
        Log.d(TAG, "loadMessages: Loading messages for group: " + groupId);
        database.getReference().child("Groups").child(groupId).child("messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.d(TAG, "onDataChange: Messages snapshot: " + snapshot);
                        messageList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            MessageModel message = dataSnapshot.getValue(MessageModel.class);
                            if (message != null) {
                                messageList.add(message);
                            }
                        }
                        Log.d(TAG, "onDataChange: Loaded " + messageList.size() + " messages");
                        chatAdapter.notifyDataSetChanged();
                        chatRecyclerView.scrollToPosition(messageList.size() - 1);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "onCancelled: Failed to load messages: " + error.getMessage());
                        Toast.makeText(GroupInboxActivity.this, "Failed to load messages", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendMessage() {
        String message = inputMessage.getText().toString().trim();
        if (!message.isEmpty()) {
            String messageId = database.getReference().child("Groups").child(groupId).child("messages").push().getKey();
            if (messageId == null) {
                Log.e(TAG, "sendMessage: Failed to generate message ID");
                return;
            }

            // Get current user's name
            database.getReference().child("user").child(currentUserId).get()
                    .addOnSuccessListener(dataSnapshot -> {
                        if (dataSnapshot.exists()) {
                            String senderName = dataSnapshot.child("username").getValue(String.class);

                            MessageModel messageModel = new MessageModel();
                            messageModel.setMessageId(messageId);
                            messageModel.setMessage(message);
                            messageModel.setUid(currentUserId);
                            messageModel.setTimestamp(System.currentTimeMillis());
                            messageModel.setGroupMessage(true);
                            messageModel.setSenderName(senderName);

                            database.getReference().child("Groups").child(groupId).child("messages").child(messageId)
                                    .setValue(messageModel)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "sendMessage: Message sent successfully");
                                        inputMessage.setText("");
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "sendMessage: Failed to send message: " + e.getMessage());
                                        Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "sendMessage: Failed to get user name: " + e.getMessage());
                        Toast.makeText(this, "Failed to get user name", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

        private List<MessageModel> messages;

        public MessageAdapter(List<MessageModel> messages) {
            this.messages = messages;
        }

        @NonNull
        @Override
        public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
            return new MessageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
            MessageModel message = messages.get(position);
            holder.messageText.setText(message.getMessage());
            
            // Set sender's name
            if (message.getSenderId() != null) {
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(message.getSenderId());
                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String senderName = snapshot.child("name").getValue(String.class);
                            holder.senderName.setText(senderName);
                            holder.senderName.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("GroupInboxActivity", "Error getting sender name: " + error.getMessage());
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

        class MessageViewHolder extends RecyclerView.ViewHolder {

            TextView messageText;
            TextView senderName;

            MessageViewHolder(View itemView) {
                super(itemView);
                messageText = itemView.findViewById(R.id.message_text);
                senderName = itemView.findViewById(R.id.sender_name);
            }
        }
    }
}
