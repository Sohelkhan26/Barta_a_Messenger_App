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
import android.widget.Button;

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
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;

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
    private CircleImageView drawerGroupImage;
    private TextView drawerGroupName;
    private RecyclerView membersRecyclerView;
    private Button addMemberButton;
    private Button leaveGroupButton;

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference groupRef;

    private String groupId;
    private String currentUserId;
    private ArrayList<MessageModel> messageList;
    private ChatAdapter chatAdapter;
    private ArrayList<String> memberList;
    private MemberAdapter memberAdapter;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;

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
        infoButton = findViewById(R.id.inboxInfo);
        groupImageView = findViewById(R.id.groupImageView);

        // Initialize drawer layout
        drawerLayout = findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(
                this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        // Set click listener for info button
        infoButton.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.closeDrawer(GravityCompat.END);
            } else {
                drawerLayout.openDrawer(GravityCompat.END);
            }
        });

        // Set group name
        groupNameTextView.setText(groupName);

        // Initialize drawer views
        drawerGroupImage = findViewById(R.id.drawerGroupImage);
        drawerGroupName = findViewById(R.id.drawerGroupName);
        membersRecyclerView = findViewById(R.id.membersRecyclerView);
        addMemberButton = findViewById(R.id.addMemberButton);
        leaveGroupButton = findViewById(R.id.leaveGroupButton);

        // Set group name in drawer
        drawerGroupName.setText(groupName);

        // Setup RecyclerView
        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList, this);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        // Setup members RecyclerView
        memberList = new ArrayList<>();
        memberAdapter = new MemberAdapter(memberList);
        membersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        membersRecyclerView.setAdapter(memberAdapter);

        // Load messages
        loadMessages();

        // Load group members
        loadGroupMembers();

        // Send message button click
        sendButton.setOnClickListener(v -> {
            sendMessage();
        });

        // Image send button click (you can implement this later)
        imageSendButton.setOnClickListener(v -> {
            // Implement image sending functionality
        });

        // Add Member button click listener
        addMemberButton.setOnClickListener(v -> {
            // TODO: Implement add member functionality
        });

        // Leave Group button click listener
        leaveGroupButton.setOnClickListener(v -> {
            // TODO: Implement leave group functionality
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

    private void loadGroupMembers() {
        database.getReference().child("Groups").child(groupId).child("members")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        memberList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            String memberId = dataSnapshot.getKey();
                            if (memberId != null) {
                                // Get member name from users node
                                database.getReference().child("user").child(memberId)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                                if (userSnapshot.exists()) {
                                                    String memberName = userSnapshot.child("username").getValue(String.class);
                                                    memberList.add(memberName);
                                                    memberAdapter.notifyDataSetChanged();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                Log.e(TAG, "Error getting member name: " + error.getMessage());
                                            }
                                        });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error loading group members: " + error.getMessage());
                    }
                });
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

    private class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {

        private List<String> members;

        public MemberAdapter(List<String> members) {
            this.members = members;
        }

        @NonNull
        @Override
        public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_member, parent, false);
            return new MemberViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
            String memberName = members.get(position);
            holder.memberName.setText(memberName);
        }

        @Override
        public int getItemCount() {
            return members.size();
        }

        class MemberViewHolder extends RecyclerView.ViewHolder {

            TextView memberName;

            MemberViewHolder(View itemView) {
                super(itemView);
                memberName = itemView.findViewById(R.id.member_name);
            }
        }
    }
}
