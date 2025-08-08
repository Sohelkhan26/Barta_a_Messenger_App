package com.example.barta_a_messenger_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;

public class ForwardContactsActivity extends AppCompatActivity implements ForwardContactAdapter.OnContactSelectListener {

    private static final String EXTRA_SENDER_ID = "sender_id";
    private static final String EXTRA_RECEIVER_ID = "receiver_id";
    private static final String EXTRA_SELECTED_MESSAGES = "selected_messages";

    private AppCompatImageView backButton;
    private TextView titleTextView;
    private TextView selectedCountTextView;
    private RecyclerView contactsRecyclerView;
    private Button forwardButton;
    private ProgressBar progressBar;
    private TextView noContactsTextView;

    private FirebaseAuth mAuth;
    private DatabaseReference database;
    private ForwardContactAdapter contactAdapter;
    private ArrayList<ForwardContact> allContacts;
    private ArrayList<ForwardContact> selectedContacts;
    private ArrayList<MessageModel> messagesToForward;

    private String senderId;
    private String receiverId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_forward_contacts);

            // Initialize Firebase
            mAuth = FirebaseAuth.getInstance();
            database = FirebaseDatabase.getInstance().getReference();

            // Get data from intent
            Intent intent = getIntent();
            senderId = intent.getStringExtra(EXTRA_SENDER_ID);
            receiverId = intent.getStringExtra(EXTRA_RECEIVER_ID);
            messagesToForward = (ArrayList<MessageModel>) intent.getSerializableExtra(EXTRA_SELECTED_MESSAGES);

            android.util.Log.d("ForwardContactsActivity", "onCreate - senderId: " + senderId
                    + ", receiverId: " + receiverId
                    + ", messages: " + (messagesToForward != null ? messagesToForward.size() : "null"));

            // Validate required data
            if (senderId == null || senderId.isEmpty()) {
                android.util.Log.e("ForwardContactsActivity", "Sender ID is missing");
                Toast.makeText(this, "Error: Missing sender information", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            if (messagesToForward == null || messagesToForward.isEmpty()) {
                android.util.Log.e("ForwardContactsActivity", "No messages to forward");
                Toast.makeText(this, "Error: No messages to forward", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Initialize lists first (before initViews which calls updateSelectedCount)
            allContacts = new ArrayList<>();
            selectedContacts = new ArrayList<>();

            // Initialize views
            initViews();

            // Set up RecyclerView
            setupRecyclerView();

            // Set up click listeners
            setupClickListeners();

            // Load contacts
            loadContacts();

        } catch (Exception e) {
            android.util.Log.e("ForwardContactsActivity", "Error in onCreate: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Error loading forward contacts", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        backButton = findViewById(R.id.imageBack);
        titleTextView = findViewById(R.id.titleText);
        selectedCountTextView = findViewById(R.id.selectedCountText);
        contactsRecyclerView = findViewById(R.id.contactsRecyclerView);
        forwardButton = findViewById(R.id.forwardButton);
        progressBar = findViewById(R.id.progressBar);
        noContactsTextView = findViewById(R.id.noContactsText);

        // Set initial text
        titleTextView.setText("Forward Messages");
        updateSelectedCount();
        forwardButton.setEnabled(false);
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        contactsRecyclerView.setLayoutManager(layoutManager);
        contactAdapter = new ForwardContactAdapter(allContacts, this);
        contactsRecyclerView.setAdapter(contactAdapter);
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> {
            finish();
        });

        forwardButton.setOnClickListener(v -> {
            if (selectedContacts.size() > 0) {
                forwardMessages();
            } else {
                Toast.makeText(this, "Please select at least one contact", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadContacts() {
        progressBar.setVisibility(View.VISIBLE);
        contactsRecyclerView.setVisibility(View.GONE);
        noContactsTextView.setVisibility(View.GONE);

        if (senderId == null) {
            Log.e("ForwardContacts", "Sender ID is null");
            showError("Unable to load contacts");
            return;
        }

        // Load contacts from Firebase
        database.child("Contacts").child(senderId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        allContacts.clear();

                        if (!snapshot.exists() || snapshot.getChildrenCount() == 0) {
                            showNoContacts();
                            return;
                        }

                        final int totalContacts = (int) snapshot.getChildrenCount();
                        final int[] processedContacts = {0};

                        for (DataSnapshot contactSnapshot : snapshot.getChildren()) {
                            String contactId = contactSnapshot.getKey();

                            // Skip the current receiver to avoid self-forwarding to same person
                            if (contactId != null && !contactId.equals(receiverId)) {
                                // Get user details from user node
                                database.child("user").child(contactId)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                                processedContacts[0]++;

                                                if (userSnapshot.exists()) {
                                                    String username = userSnapshot.child("username").getValue(String.class);
                                                    String profilePic = userSnapshot.child("profilePic").getValue(String.class);
                                                    String status = userSnapshot.child("status").getValue(String.class);

                                                    if (username != null) {
                                                        ForwardContact contact = new ForwardContact(
                                                                contactId,
                                                                username,
                                                                profilePic != null ? profilePic : "",
                                                                status != null ? status : "Available",
                                                                false
                                                        );
                                                        allContacts.add(contact);
                                                    }
                                                }

                                                // Update UI when all contacts are processed
                                                if (processedContacts[0] >= totalContacts - 1) { // -1 because we skip receiverId
                                                    updateContactsList();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                processedContacts[0]++;
                                                Log.e("ForwardContacts", "Error loading user: " + error.getMessage());

                                                if (processedContacts[0] >= totalContacts - 1) {
                                                    updateContactsList();
                                                }
                                            }
                                        });
                            } else {
                                processedContacts[0]++;
                                if (processedContacts[0] >= totalContacts - 1) {
                                    updateContactsList();
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("ForwardContacts", "Error loading contacts: " + error.getMessage());
                        showError("Failed to load contacts");
                    }
                });
    }

    private void updateContactsList() {
        runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);

            if (allContacts.isEmpty()) {
                showNoContacts();
            } else {
                contactsRecyclerView.setVisibility(View.VISIBLE);
                contactAdapter.notifyDataSetChanged();
            }
        });
    }

    private void showNoContacts() {
        progressBar.setVisibility(View.GONE);
        contactsRecyclerView.setVisibility(View.GONE);
        noContactsTextView.setVisibility(View.VISIBLE);
        noContactsTextView.setText("No contacts available for forwarding");
    }

    private void showError(String message) {
        progressBar.setVisibility(View.GONE);
        contactsRecyclerView.setVisibility(View.GONE);
        noContactsTextView.setVisibility(View.VISIBLE);
        noContactsTextView.setText(message);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onContactSelected(ForwardContact contact) {
        if (!selectedContacts.contains(contact)) {
            selectedContacts.add(contact);
            contact.setSelected(true);
            updateSelectedCount();
            forwardButton.setEnabled(true);
        }
    }

    @Override
    public void onContactDeselected(ForwardContact contact) {
        selectedContacts.remove(contact);
        contact.setSelected(false);
        updateSelectedCount();
        forwardButton.setEnabled(selectedContacts.size() > 0);
    }

    private void updateSelectedCount() {
        if (selectedContacts.size() == 0) {
            selectedCountTextView.setText("Select contacts to forward messages");
            selectedCountTextView.setTextColor(getResources().getColor(android.R.color.darker_gray));
        } else {
            selectedCountTextView.setText(selectedContacts.size() + " contact(s) selected");
            selectedCountTextView.setTextColor(getResources().getColor(R.color.primary_blue));
        }
    }

    private void forwardMessages() {
        if (messagesToForward == null || messagesToForward.isEmpty()) {
            Toast.makeText(this, "No messages to forward", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress
        forwardButton.setEnabled(false);
        forwardButton.setText("Forwarding...");

        final int totalForwards = selectedContacts.size() * messagesToForward.size();
        final int[] completedForwards = {0};

        for (ForwardContact contact : selectedContacts) {
            for (MessageModel message : messagesToForward) {
                forwardMessageTo(contact.getUid(), message, () -> {
                    completedForwards[0]++;

                    // Check if all forwards are complete
                    if (completedForwards[0] >= totalForwards) {
                        runOnUiThread(() -> {
                            Toast.makeText(ForwardContactsActivity.this,
                                    "Messages forwarded successfully to " + selectedContacts.size() + " contact(s)",
                                    Toast.LENGTH_LONG).show();

                            // Set result and finish
                            setResult(RESULT_OK);
                            finish();
                        });
                    }
                });
            }
        }
    }

    private void forwardMessageTo(String recipientUID, MessageModel message, Runnable onComplete) {
        if (recipientUID == null) {
            Log.e("ForwardContacts", "Recipient UID is null");
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }

        try {
            Log.d("ForwardContacts", "Forwarding message to UID: " + recipientUID);

            // For forwarding, keep the original encrypted message as is
            // No need to re-encrypt
            final String encryptedMsg = message.getMessage();

            // Generate key for Firebase
            String key = database.child("chats")
                    .child(recipientUID)
                    .child(senderId)
                    .push().getKey();

            if (key == null) {
                if (onComplete != null) {
                    onComplete.run();
                }
                return;
            }

            MessageModel forwardedMessage = new MessageModel(senderId, encryptedMsg);
            forwardedMessage.setMessageId(key);
            forwardedMessage.setTimestamp(new Date().getTime());
            forwardedMessage.setIsNotified("no");
            forwardedMessage.setMessageType(message.getMessageType());

            // Save to recipient's chat (received message)
            database.child("chats")
                    .child(recipientUID)
                    .child(senderId)
                    .child(key)
                    .setValue(forwardedMessage)
                    .addOnSuccessListener(unused -> {
                        // Update recipient's contacts
                        updateContactInfo(recipientUID, senderId, encryptedMsg, "", forwardedMessage.getTimestamp(), "false");

                        // Save to sender's chat (sent message)
                        database.child("chats")
                                .child(senderId)
                                .child(recipientUID)
                                .child(key)
                                .setValue(forwardedMessage)
                                .addOnSuccessListener(v -> {
                                    // Update sender's contacts
                                    updateContactInfo(senderId, recipientUID, encryptedMsg, "You", forwardedMessage.getTimestamp(), "true");
                                    if (onComplete != null) {
                                        onComplete.run();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("ForwardContacts", "Error saving to sender: " + e.getMessage());
                                    if (onComplete != null) {
                                        onComplete.run();
                                    }
                                });
                    })
                    .addOnFailureListener(e -> {
                        Log.e("ForwardContacts", "Error saving to recipient: " + e.getMessage());
                        if (onComplete != null) {
                            onComplete.run();
                        }
                    });

        } catch (Exception e) {
            Log.e("ForwardContacts", "Error forwarding message: " + e.getMessage());
            if (onComplete != null) {
                onComplete.run();
            }
        }
    }

    private void updateContactInfo(String recipientUID, String senderId, String lastMessage, String lastSenderName, Long messageTime, String lastMessageSeen) {
        database.child("Contacts")
                .child(recipientUID)
                .child(senderId)
                .child("last_message")
                .setValue(lastMessage);

        database.child("Contacts")
                .child(recipientUID)
                .child(senderId)
                .child("last_sender_name")
                .setValue(lastSenderName);

        database.child("Contacts")
                .child(recipientUID)
                .child(senderId)
                .child("message_time")
                .setValue(messageTime);

        database.child("Contacts")
                .child(recipientUID)
                .child(senderId)
                .child("last_message_seen")
                .setValue(lastMessageSeen);
    }

    // Static method to start this activity
    public static void startForForward(AppCompatActivity activity, String senderId, String receiverId, ArrayList<MessageModel> selectedMessages, int requestCode) {
        try {
            if (activity == null) {
                android.util.Log.e("ForwardContactsActivity", "Activity is null");
                return;
            }

            if (senderId == null || senderId.isEmpty()) {
                android.util.Log.e("ForwardContactsActivity", "Sender ID is null or empty");
                return;
            }

            if (selectedMessages == null || selectedMessages.isEmpty()) {
                android.util.Log.e("ForwardContactsActivity", "Selected messages is null or empty");
                return;
            }

            android.util.Log.d("ForwardContactsActivity", "Starting activity with senderId: " + senderId
                    + ", receiverId: " + receiverId + ", messages: " + selectedMessages.size());

            Intent intent = new Intent(activity, ForwardContactsActivity.class);
            intent.putExtra(EXTRA_SENDER_ID, senderId);
            intent.putExtra(EXTRA_RECEIVER_ID, receiverId);
            intent.putExtra(EXTRA_SELECTED_MESSAGES, selectedMessages);
            activity.startActivityForResult(intent, requestCode);
        } catch (Exception e) {
            android.util.Log.e("ForwardContactsActivity", "Error starting activity: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
    }
}
