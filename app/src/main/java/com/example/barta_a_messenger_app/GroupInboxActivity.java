package com.example.barta_a_messenger_app;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import android.app.AlertDialog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import androidx.appcompat.widget.AppCompatImageView;
import de.hdodenhof.circleimageview.CircleImageView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;

public class GroupInboxActivity extends AppCompatActivity implements ChatAdapter.OnMessageSelectListener {

    private static final String TAG = "GroupInboxActivity";
    private TextView groupNameTextView;
    private RecyclerView chatRecyclerView;
    private EditText inputMessage;
    private AppCompatImageView sendButton;
    private ImageButton imageSendButton;
    private AppCompatImageView backButton;
    private AppCompatImageView infoButton;
    private CircleImageView groupImageView;
    private RecyclerView membersRecyclerView;
    private Button membersButton;
    private boolean isMembersExpanded = false;

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference groupRef;

    private String groupId;
    private String currentUserId;
    private ArrayList<String> currentMembers = new ArrayList<>();
    private ArrayList<MessageModel> messageList;
    private ChatAdapter chatAdapter;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;

    private Button addMemberButton;
    private ContactAdapter contactAdapter;
    private ArrayList<Contact> selectedContacts = new ArrayList<>();

    private Button leaveGroupButton;

    private ArrayList<MessageModel> selectedMessages;
    private LinearLayout actionButtonsLayout;
    private Button forwardButton;
    private Button deleteButton;

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
        membersRecyclerView = findViewById(R.id.membersRecyclerView);
        membersButton = findViewById(R.id.membersButton);

        // Initialize drawer layout
        drawerLayout = findViewById(R.id.drawer_layout);
        Log.d(TAG, "onCreate: DrawerLayout initialized: " + (drawerLayout != null));

        drawerToggle = new ActionBarDrawerToggle(
                this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        // Set click listener for info button
        infoButton.setOnClickListener(v -> {
            Log.d(TAG, "Info button clicked");
            if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                Log.d(TAG, "Closing drawer");
                drawerLayout.closeDrawer(GravityCompat.END);
            } else {
                Log.d(TAG, "Opening drawer");
                drawerLayout.openDrawer(GravityCompat.END);
                // Force layout update
                drawerLayout.requestLayout();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        // Set members button click listener
        membersButton.setOnClickListener(v -> {
            isMembersExpanded = !isMembersExpanded;
            if (isMembersExpanded) {
                membersRecyclerView.setVisibility(View.VISIBLE);
                membersButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_expand_less, 0);
                loadGroupMembers();
            } else {
                membersRecyclerView.setVisibility(View.GONE);
                membersButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_expand_more, 0);
            }
        });

        // Set group name
        groupNameTextView.setText(groupName);

        // Setup RecyclerView
        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList, this);
        chatAdapter.setOnMessageSelectListener(this);
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

        // Set group name and image in drawer
        TextView drawerGroupName = findViewById(R.id.drawerGroupName);
        CircleImageView drawerGroupImage = findViewById(R.id.drawerGroupImage);

        Log.d(TAG, "Setting group name in drawer: " + groupName);
        drawerGroupName.setText(groupName);
        // You can set the group image here if you have it

        // Initialize Add Member views
        addMemberButton = findViewById(R.id.addMemberButton);

        // Set up Add Member button click
        addMemberButton.setOnClickListener(v -> {
            showContactsDialog();
        });

        // Initialize Leave Group button
        leaveGroupButton = findViewById(R.id.leaveGroupButton);
        leaveGroupButton.setOnClickListener(v -> showLeaveGroupDialog());

        // Initialize action buttons
        actionButtonsLayout = findViewById(R.id.actionButtonsLayout);
        forwardButton = findViewById(R.id.forwardButton);
        deleteButton = findViewById(R.id.deleteButton);

        // Set click listeners for action buttons
        forwardButton.setOnClickListener(v -> {
            if (selectedMessages != null && !selectedMessages.isEmpty()) {
                Log.d(TAG, "Forwarding " + selectedMessages.size() + " messages");
                for (MessageModel message : selectedMessages) {
                    Log.d("SelectedMessages",
                            "Message ID: " + message.getMessageId()
                            + ", Content: " + message.getMessage()
                            + ", Type: " + message.getMessageType()
                            + ", Sender: " + message.getUid()
                            + ", Timestamp: " + message.getTimestamp()
                    );
                }
                showContactsDialog();
            } else {
                Toast.makeText(GroupInboxActivity.this, "No messages selected", Toast.LENGTH_SHORT).show();
            }
        });

        deleteButton.setOnClickListener(v -> {
            if (selectedMessages != null && !selectedMessages.isEmpty()) {
                Log.d(TAG, "Deleting " + selectedMessages.size() + " messages");
                for (MessageModel message : selectedMessages) {
                    deleteMessage(message);
                }
                actionButtonsLayout.setVisibility(View.GONE);
                forwardButton.setVisibility(View.GONE);
                deleteButton.setVisibility(View.GONE);
                chatAdapter.clearSelection();
            }
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
        Log.d(TAG, "loadGroupMembers: Loading members for group: " + groupId);
        DatabaseReference groupMembersRef = database.getReference().child("Groups").child(groupId).child("members");
        groupMembersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> memberIds = new ArrayList<>();
                Log.d(TAG, "onDataChange: Members snapshot: " + snapshot);

                // Get the members array directly
                ArrayList<String> members = snapshot.getValue(new GenericTypeIndicator<ArrayList<String>>() {
                });
                if (members != null) {
                    memberIds.addAll(members);
                    currentMembers.clear();
                    currentMembers.addAll(members);
                    for (String memberId : members) {
                        Log.d(TAG, "Found member ID: " + memberId);
                    }
                }

                Log.d(TAG, "Total members found: " + memberIds.size());
                setupMembersRecyclerView(memberIds);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load group members: " + error.getMessage());
                Toast.makeText(GroupInboxActivity.this, "Failed to load group members", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupMembersRecyclerView(List<String> memberIds) {
        Log.d(TAG, "setupMembersRecyclerView: Setting up adapter with " + memberIds.size() + " members");
        MembersAdapter membersAdapter = new MembersAdapter(memberIds);
        membersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        membersRecyclerView.setAdapter(membersAdapter);
        membersRecyclerView.setVisibility(View.VISIBLE);
    }

    private class MembersAdapter extends RecyclerView.Adapter<MembersAdapter.MemberViewHolder> {

        private List<String> memberIds;

        public MembersAdapter(List<String> memberIds) {
            this.memberIds = memberIds;
            Log.d(TAG, "MembersAdapter created with " + memberIds.size() + " members");
        }

        @NonNull
        @Override
        public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_member, parent, false);
            return new MemberViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
            String memberId = memberIds.get(position);
            Log.d(TAG, "Binding member at position " + position + " with ID: " + memberId);
            DatabaseReference userRef = database.getReference().child("user").child(memberId);
            Log.d(TAG, "Fetching user data from path: " + userRef.toString());
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Log.d(TAG, "User data snapshot: " + snapshot.toString());
                    if (snapshot.exists()) {
                        String userName = snapshot.child("username").getValue(String.class);
                        Log.d(TAG, "Username from snapshot: " + userName);
                        if (userName != null) {
                            Log.d(TAG, "Setting username for member " + memberId + ": " + userName);
                            holder.memberName.setText(userName);
                        } else {
                            Log.e(TAG, "Username is null for member: " + memberId);
                            holder.memberName.setText("Unknown User");
                        }
                    } else {
                        Log.e(TAG, "No user data found for member: " + memberId);
                        holder.memberName.setText("Unknown User");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Failed to load member details: " + error.getMessage());
                    holder.memberName.setText("Error Loading User");
                }
            });
        }

        @Override
        public int getItemCount() {
            return memberIds.size();
        }

        class MemberViewHolder extends RecyclerView.ViewHolder {

            TextView memberName;

            MemberViewHolder(View itemView) {
                super(itemView);
                memberName = itemView.findViewById(R.id.member_name);
            }
        }
    }

    private void showContactsDialog() {
        if (currentUserId == null) {
            Log.e("ContactsDebug", "Current user ID not found!");
            return;
        }

        database.getReference()
                .child("Contacts")
                .child(currentUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ArrayList<String> userNames = new ArrayList<>();
                        ArrayList<String> userIds = new ArrayList<>();

                        for (DataSnapshot contactSnapshot : snapshot.getChildren()) {
                            String contactId = contactSnapshot.getKey();

                            if (contactId != null) {
                                database.getReference()
                                        .child("user")
                                        .child(contactId)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                                if (userSnapshot.exists()) {
                                                    String username = userSnapshot.child("username").getValue(String.class);
                                                    userNames.add(username);
                                                    userIds.add(contactId);

                                                    if (userNames.size() == snapshot.getChildrenCount()) {
                                                        showForwardDialogWithContacts(userNames, userIds);
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                Log.e("ContactsDebug", "Error: " + error.getMessage());
                                            }
                                        });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("ContactsDebug", "Error: " + error.getMessage());
                    }
                });
    }

    private void showForwardDialogWithContacts(ArrayList<String> userNames, ArrayList<String> userIds) {
        String[] names = userNames.toArray(new String[0]);
        boolean[] checkedItems = new boolean[names.length];

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Forward Messages");

        builder.setMultiChoiceItems(names, checkedItems, (dialog, which, isChecked) -> {
            String selectedUsername = names[which];
            String selectedUID = userIds.get(which);

            if (isChecked) {
                Log.d("ForwardDebug", "Selected user: " + selectedUsername + " (UID: " + selectedUID + ")");
            } else {
                Log.d("ForwardDebug", "Deselected user: " + selectedUsername + " (UID: " + selectedUID + ")");
            }
        });

        builder.setPositiveButton("Send", (dialog, which) -> {
            for (int i = 0; i < checkedItems.length; i++) {
                if (checkedItems[i]) {
                    String recipientUID = userIds.get(i);
                    forwardMessagesTo(recipientUID);
                }
            }

            actionButtonsLayout.setVisibility(View.GONE);
            forwardButton.setVisibility(View.GONE);
            deleteButton.setVisibility(View.GONE);
            chatAdapter.clearSelection();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void forwardMessagesTo(String recipientUID) {
        if (recipientUID == null) {
            Log.e("ForwardDebug", "Recipient UID is null");
            return;
        }

        for (MessageModel message : selectedMessages) {
            try {
                Log.d("ForwardDebug", "Forwarding message to UID: " + recipientUID);

                String encryptedMsg = CryptoHelper.encrypt("H@rrY_p0tter_106", message.getMessage());

                String key = database.getReference().child("chats")
                        .child(recipientUID)
                        .child(currentUserId)
                        .push().getKey();

                if (key == null) {
                    continue;
                }

                MessageModel forwardedMessage = new MessageModel(currentUserId, encryptedMsg);
                forwardedMessage.setMessageId(key);
                forwardedMessage.setTimestamp(new Date().getTime());
                forwardedMessage.setIsNotified("no");
                forwardedMessage.setMessageType(message.getMessageType());

                database.getReference().child("chats")
                        .child(recipientUID)
                        .child(currentUserId)
                        .child(key)
                        .setValue(forwardedMessage)
                        .addOnSuccessListener(unused -> {
                            updateContactInfo(recipientUID, currentUserId, encryptedMsg, "", forwardedMessage.getTimestamp(), "false");

                            database.getReference().child("chats")
                                    .child(currentUserId)
                                    .child(recipientUID)
                                    .child(key)
                                    .setValue(forwardedMessage)
                                    .addOnSuccessListener(v -> {
                                        updateContactInfo(currentUserId, recipientUID, encryptedMsg, "You", forwardedMessage.getTimestamp(), "true");
                                    });
                        })
                        .addOnFailureListener(e -> {
                            Log.e("ForwardDebug", "Error: " + e.getMessage());
                        });

            } catch (Exception e) {
                Log.e("ForwardDebug", "Error: " + e.getMessage());
            }
        }
    }

    private void updateContactInfo(String recipientUID, String senderId, String lastMessage, String lastSenderName, Long messageTime, String lastMessageSeen) {
        database.getReference().child("Contacts")
                .child(recipientUID)
                .child(senderId)
                .child("last_message")
                .setValue(lastMessage);

        database.getReference().child("Contacts")
                .child(recipientUID)
                .child(senderId)
                .child("last_sender_name")
                .setValue(lastSenderName);

        database.getReference().child("Contacts")
                .child(recipientUID)
                .child(senderId)
                .child("message_time")
                .setValue(messageTime);

        database.getReference().child("Contacts")
                .child(recipientUID)
                .child(senderId)
                .child("last_message_seen")
                .setValue(lastMessageSeen);

        database.getReference().child("Contacts")
                .child(senderId)
                .child(recipientUID)
                .child("last_message")
                .setValue(lastMessage);

        database.getReference().child("Contacts")
                .child(senderId)
                .child(recipientUID)
                .child("last_sender_name")
                .setValue(lastSenderName);

        database.getReference().child("Contacts")
                .child(senderId)
                .child(recipientUID)
                .child("message_time")
                .setValue(messageTime);

        database.getReference().child("Contacts")
                .child(senderId)
                .child(recipientUID)
                .child("last_message_seen")
                .setValue(lastMessageSeen);
    }

    private void deleteMessage(MessageModel message) {
        database.getReference()
                .child("Groups")
                .child(groupId)
                .child("messages")
                .child(message.getMessageId())
                .removeValue()
                .addOnSuccessListener(aVoid -> {
                    messageList.remove(message);
                    chatAdapter.notifyDataSetChanged();
                    Toast.makeText(GroupInboxActivity.this, "Message deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(GroupInboxActivity.this, "Failed to delete message", Toast.LENGTH_SHORT).show();
                });
    }

    private void showLeaveGroupDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Leave Group")
                .setMessage("Are you sure you want to leave this group? Your chat history will be deleted.")
                .setPositiveButton("Leave", (dialog, which) -> leaveGroup())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void leaveGroup() {
        // Remove user from group members
        DatabaseReference groupRef = database.getReference().child("Groups").child(groupId);
        groupRef.child("members").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> members = snapshot.getValue(new GenericTypeIndicator<ArrayList<String>>() {
                });
                if (members != null) {
                    members.remove(currentUserId);
                    groupRef.child("members").setValue(members);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(GroupInboxActivity.this, "Failed to leave group", Toast.LENGTH_SHORT).show();
            }
        });

        // Delete group from user's chats
        database.getReference().child("Chats")
                .child(currentUserId)
                .child(groupId)
                .removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(GroupInboxActivity.this, "Left group successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(GroupInboxActivity.this, "Failed to leave group", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onMessageSelectModeActivated() {
        Log.d(TAG, "Message selection mode activated");
        if (selectedMessages == null) {
            selectedMessages = new ArrayList<>();
        }
        actionButtonsLayout.setVisibility(View.VISIBLE);
        forwardButton.setVisibility(View.VISIBLE);
        deleteButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onMessageSelected(ArrayList<MessageModel> messages) {
        Log.d(TAG, "Messages selected: " + messages.size());
        selectedMessages = messages;
        if (messages.size() > 0) {
            actionButtonsLayout.setVisibility(View.VISIBLE);
            forwardButton.setVisibility(View.VISIBLE);

            // Check if all selected messages are from current user
            boolean allOwnMessages = true;
            for (MessageModel message : messages) {
                if (!message.getUid().equals(currentUserId)) {
                    allOwnMessages = false;
                    break;
                }
            }

            // Only show delete button if all selected messages are from current user
            deleteButton.setVisibility(allOwnMessages ? View.VISIBLE : View.GONE);
        } else {
            Log.d(TAG, "No messages selected, hiding action buttons");
            actionButtonsLayout.setVisibility(View.GONE);
            forwardButton.setVisibility(View.GONE);
            deleteButton.setVisibility(View.GONE);
            chatAdapter.clearSelection();
        }
    }

    @Override
    public void onBackPressed() {
        if (actionButtonsLayout.getVisibility() == View.VISIBLE) {
            // Clear selection and hide buttons
            actionButtonsLayout.setVisibility(View.GONE);
            forwardButton.setVisibility(View.GONE);
            deleteButton.setVisibility(View.GONE);
            chatAdapter.clearSelection();
        } else {
            super.onBackPressed();
        }
    }

    public void onContactSelected(Contact contact) {
        Log.d(TAG, "Contact selected: " + contact.getFull_name());
        selectedContacts.add(contact);
        // You can add additional logic here if needed
    }

    public void onContactDeselected(Contact contact) {
        Log.d(TAG, "Contact deselected: " + contact.getFull_name());
        selectedContacts.remove(contact);
        // You can add additional logic here if needed
    }
}
