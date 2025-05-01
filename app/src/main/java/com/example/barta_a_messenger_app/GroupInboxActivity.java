package com.example.barta_a_messenger_app;

import android.app.Dialog;
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
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

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
        // Create dialog
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_members);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // Initialize views
        RecyclerView dialogContactsRecyclerView = dialog.findViewById(R.id.contacts_recycler_view);
        Button addButton = dialog.findViewById(R.id.add_button);
        Button cancelButton = dialog.findViewById(R.id.cancel_button);
        TextView noContactsText = dialog.findViewById(R.id.no_contacts_text);
        View buttonsContainer = dialog.findViewById(R.id.buttons_container);

        // Set up RecyclerView
        dialogContactsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        contactAdapter = new ContactAdapter(new ArrayList<>(), GroupInboxActivity.this, true);
        dialogContactsRecyclerView.setAdapter(contactAdapter);

        // Load contacts
        database.getReference().child("Contacts").child(currentUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ArrayList<Contact> contacts = new ArrayList<>();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Contact contact = dataSnapshot.getValue(Contact.class);
                            if (contact != null) {
                                // Check if contact is already in the group
                                if (!isContactInGroup(contact.getUid())) {
                                    contacts.add(contact);
                                }
                            }
                        }
                        
                        if (contacts.isEmpty()) {
                            // No contacts to add
                            noContactsText.setVisibility(View.VISIBLE);
                            dialogContactsRecyclerView.setVisibility(View.GONE);
                            buttonsContainer.setVisibility(View.GONE);
                        } else {
                            // Show contacts and buttons
                            noContactsText.setVisibility(View.GONE);
                            dialogContactsRecyclerView.setVisibility(View.VISIBLE);
                            buttonsContainer.setVisibility(View.VISIBLE);
                            contactAdapter.updateContacts(contacts);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(GroupInboxActivity.this, "Failed to load contacts", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });

        // Set up buttons
        addButton.setOnClickListener(v -> {
            addSelectedMembers();
            dialog.dismiss();
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private boolean isContactInGroup(String contactId) {
        // Check if the contact is already a member of the group
        for (String memberId : currentMembers) {
            if (memberId.equals(contactId)) {
                return true;
            }
        }
        return false;
    }

    private void addSelectedMembers() {
        if (selectedContacts.isEmpty()) {
            Toast.makeText(this, "Please select contacts to add", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference groupRef = database.getReference().child("Groups").child(groupId);

        // Get current members first
        groupRef.child("members").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> currentMembers = new ArrayList<>();

                // Get existing members
                for (DataSnapshot memberSnapshot : snapshot.getChildren()) {
                    String memberId = memberSnapshot.getValue(String.class);
                    if (memberId != null) {
                        currentMembers.add(memberId);
                    }
                }

                // Add new members
                for (Contact contact : selectedContacts) {
                    if (!currentMembers.contains(contact.getUid())) {
                        currentMembers.add(contact.getUid());

                        // Add group to new member's chats
                        Map<String, Object> chatData = new HashMap<>();
                        chatData.put("groupId", groupId);
                        chatData.put("groupName", groupNameTextView.getText().toString());
                        chatData.put("lastMessage", "");
                        chatData.put("timestamp", System.currentTimeMillis());

                        database.getReference().child("Chats")
                                .child(contact.getUid())
                                .child(groupId)
                                .setValue(chatData);
                    }
                }

                // Update group members
                groupRef.child("members").setValue(currentMembers)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(GroupInboxActivity.this, "Members added successfully", Toast.LENGTH_SHORT).show();
                            loadGroupMembers(); // Refresh members list
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(GroupInboxActivity.this, "Failed to add members", Toast.LENGTH_SHORT).show();
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(GroupInboxActivity.this, "Failed to add members", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onContactSelected(Contact contact) {
        if (!selectedContacts.contains(contact)) {
            selectedContacts.add(contact);
        }
    }

    public void onContactDeselected(Contact contact) {
        selectedContacts.remove(contact);
    }
}
