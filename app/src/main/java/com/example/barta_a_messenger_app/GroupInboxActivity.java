package com.example.barta_a_messenger_app;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
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

import androidx.appcompat.app.AlertDialog;

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

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;

import java.util.Collections;
import java.util.Date;

public class GroupInboxActivity extends AppCompatActivity {

    private static final String TAG = "GroupInboxActivity";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static final int RC_AUTHORIZE_DRIVE = 10943;
    private static final int REQUEST_CODE_SIGN_IN = 1;

    private TextView groupNameTextView;
    private RecyclerView chatRecyclerView;
    private EditText inputMessage;
    private AppCompatImageView sendButton;
    private ImageButton imageSendButton;
    private ImageButton voiceSendButton;
    private AppCompatImageView backButton;
    private AppCompatImageView infoButton;
    private CircleImageView groupImageView;
    private RecyclerView membersRecyclerView;
    private Button membersButton;
    private boolean isMembersExpanded = false;

    // Voice recording variables
    private MediaRecorder mediaRecorder;
    private String voiceFileName;
    private boolean isRecording = false;
    private Uri voiceUri;
    private String encryptedMessage;

    // Google Drive variables
    private DriveServiceHelper driveServiceHelper;
    Scope ACCESS_DRIVE_SCOPE = new Scope(Scopes.DRIVE_FILE);
    Scope SCOPE_EMAIL = new Scope(Scopes.EMAIL);

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
        voiceSendButton = findViewById(R.id.voice_send_button);
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

        // Voice send button click
        voiceSendButton.setOnClickListener(v -> {
            if (!isRecording) {
                if (checkAudioPermission()) {
                    startRecording();
                } else {
                    requestAudioPermission();
                }
            } else {
                stopRecording();
            }
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

    // Voice Recording Methods
    private boolean checkAudioPermission() {
        boolean recordAudio = checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
        boolean modifyAudio = checkSelfPermission(android.Manifest.permission.MODIFY_AUDIO_SETTINGS) == PackageManager.PERMISSION_GRANTED;

        android.util.Log.d("VoiceRecording", "Audio permissions - Record: " + recordAudio + ", Modify: " + modifyAudio);
        return recordAudio && modifyAudio;
    }

    private void requestAudioPermission() {
        String[] permissions = {
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.MODIFY_AUDIO_SETTINGS
        };

        android.util.Log.d("VoiceRecording", "Requesting audio permissions");
        requestPermissions(permissions, REQUEST_RECORD_AUDIO_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                android.util.Log.d("VoiceRecording", "All audio permissions granted");
                Toast.makeText(this, "🎤 Audio permissions granted. You can now record voice messages.", Toast.LENGTH_LONG).show();
            } else {
                android.util.Log.e("VoiceRecording", "Audio permissions denied");
                Toast.makeText(this, "❌ Audio permissions denied. Voice recording requires microphone access.", Toast.LENGTH_LONG).show();

                // Show explanation dialog
                new AlertDialog.Builder(this)
                        .setTitle("Microphone Permission Required")
                        .setMessage("To send voice messages, this app needs access to your microphone. Please grant permission in Settings > Apps > Permissions.")
                        .setPositiveButton("Settings", (dialog, which) -> {
                            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            intent.setData(Uri.parse("package:" + getPackageName()));
                            startActivity(intent);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        }
    }

    private void startRecording() {
        try {
            // Create file name for voice recording in M4A format
            voiceFileName = getExternalCacheDir().getAbsolutePath() + "/voice_message_" + System.currentTimeMillis() + ".m4a";

            // Initialize MediaRecorder with high-quality settings
            mediaRecorder = new MediaRecorder();

            // Use VOICE_RECOGNITION for better microphone sensitivity
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION);

            // Use MPEG_4 format (creates M4A files with AAC audio)
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setOutputFile(voiceFileName);

            // Use AAC encoder (creates high-quality M4A files)
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

            // Set high quality audio settings
            mediaRecorder.setAudioSamplingRate(44100);     // CD quality
            mediaRecorder.setAudioEncodingBitRate(192000); // Higher bitrate for better quality
            mediaRecorder.setAudioChannels(1);             // Mono for voice

            android.util.Log.d("VoiceRecording", "Starting M4A recording with file: " + voiceFileName);

            mediaRecorder.prepare();
            mediaRecorder.start();

            isRecording = true;

            // Update UI to show recording state
            voiceSendButton.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_red_light));
            Toast.makeText(this, "🎤 Recording High Quality Audio... Speak clearly. Tap again to stop", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
            android.util.Log.e("VoiceRecording", "Error starting M4A recording: " + e.getMessage());
            Toast.makeText(this, "Error starting recording: " + e.getMessage(), Toast.LENGTH_LONG).show();

            // Clean up if failed
            if (mediaRecorder != null) {
                try {
                    mediaRecorder.release();
                    mediaRecorder = null;
                } catch (Exception ex) {
                    android.util.Log.e("VoiceRecording", "Error releasing MediaRecorder: " + ex.getMessage());
                }
            }
            isRecording = false;
        }
    }

    private void stopRecording() {
        try {
            if (mediaRecorder != null && isRecording) {
                android.util.Log.d("VoiceRecording", "Stopping recording...");

                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;

                isRecording = false;

                // Restore original button color
                voiceSendButton.setBackgroundTintList(getResources().getColorStateList(android.R.color.white));

                // Check if file was created and has content
                java.io.File voiceFile = new java.io.File(voiceFileName);
                if (voiceFile.exists() && voiceFile.length() > 0) {
                    android.util.Log.d("VoiceRecording", "Voice file created successfully. Size: " + voiceFile.length() + " bytes");

                    // Create URI from file path and upload to Google Drive
                    voiceUri = Uri.fromFile(voiceFile);

                    Toast.makeText(this, "✅ Recording stopped (" + (voiceFile.length() / 1024) + "KB). Uploading voice message...", Toast.LENGTH_LONG).show();
                    uploadVoiceMessageToDrive();
                } else {
                    android.util.Log.e("VoiceRecording", "Voice file not created or empty");
                    Toast.makeText(this, "Recording failed - no audio captured. Please check microphone permissions.", Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            android.util.Log.e("VoiceRecording", "Error stopping recording: " + e.getMessage());
            Toast.makeText(this, "Error stopping recording: " + e.getMessage(), Toast.LENGTH_LONG).show();

            // Clean up
            isRecording = false;
            voiceSendButton.setBackgroundTintList(getResources().getColorStateList(android.R.color.white));

            if (mediaRecorder != null) {
                try {
                    mediaRecorder.release();
                    mediaRecorder = null;
                } catch (Exception ex) {
                    android.util.Log.e("VoiceRecording", "Error releasing MediaRecorder in cleanup: " + ex.getMessage());
                }
            }
        }
    }

    private void uploadVoiceMessageToDrive() {
        checkForGooglePermissions();
        if (driveServiceHelper == null) {
            Toast.makeText(this, "Drive Service Helper is null. Setting up Drive connection...", Toast.LENGTH_SHORT).show();
            driveSetUp();
            // Try again after setup
            if (driveServiceHelper == null) {
                Toast.makeText(this, "Failed to initialize Google Drive. Please try again.", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        uploadVoiceMessage();
    }

    private void driveSetUp() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(ACCESS_DRIVE_SCOPE)
                .build();

        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null && account.getGrantedScopes().contains(ACCESS_DRIVE_SCOPE)) {
            android.util.Log.d(TAG, "driveSetUp: " + account.getEmail());
            GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(this, Collections.singleton(DriveScopes.DRIVE_FILE));
            credential.setSelectedAccount(account.getAccount());

            Drive googleDriveService = new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), credential)
                    .setApplicationName("Barta Messenger")
                    .build();

            driveServiceHelper = new DriveServiceHelper(googleDriveService, this);
        }
    }

    private void checkForGooglePermissions() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if (account != null && account.getGrantedScopes().contains(ACCESS_DRIVE_SCOPE)) {
            return;
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(ACCESS_DRIVE_SCOPE)
                .build();

        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
        startActivityForResult(googleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SIGN_IN) {
            handleSignInResult(data);
        }
    }

    private void handleSignInResult(Intent result) {
        GoogleSignIn.getSignedInAccountFromIntent(result)
                .addOnSuccessListener(googleAccount -> {
                    android.util.Log.d(TAG, "onActivityResult: " + googleAccount.getEmail());
                    GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(this, Collections.singleton(DriveScopes.DRIVE_FILE));
                    credential.setSelectedAccount(googleAccount.getAccount());

                    Drive googleDriveService = new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), credential)
                            .setApplicationName("Barta Messenger")
                            .build();

                    driveServiceHelper = new DriveServiceHelper(googleDriveService, GroupInboxActivity.this);
                })
                .addOnFailureListener(exception -> android.util.Log.e(TAG, "Unable to sign in.", exception));
    }

    private void uploadVoiceMessage() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if (account == null) {
            Toast.makeText(this, "No Google account signed in. Cannot upload voice message.", Toast.LENGTH_LONG).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading Voice Message...");
        progressDialog.show();

        try {
            // Upload voice file to Google Drive
            Task<File> uploadTask = driveServiceHelper.uploadFile(voiceUri, "voice_message_" + System.currentTimeMillis() + ".m4a");

            uploadTask.addOnCompleteListener(new OnCompleteListener<File>() {
                @Override
                public void onComplete(@NonNull Task<File> task) {
                    if (task.isSuccessful()) {
                        // Get file ID and construct download URL
                        File uploadedFile = task.getResult();
                        String fileId = uploadedFile.getId();
                        String downloadUrl = "https://drive.google.com/uc?id=" + fileId;

                        // Encrypt the download URL
                        try {
                            encryptedMessage = CryptoHelper.encrypt("H@rrY_p0tter_106", downloadUrl);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }

                        // Create message model for voice message
                        MessageModel model = new MessageModel(currentUserId, encryptedMessage, "voice");
                        model.setTimestamp(new Date().getTime());
                        model.setGroupMessage(true);

                        // Get sender name
                        database.getReference().child("user").child(currentUserId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        String senderName = snapshot.child("username").getValue(String.class);
                                        model.setSenderName(senderName);

                                        // Push message to Firebase
                                        String key = database.getReference().child("Groups")
                                                .child(groupId)
                                                .child("messages")
                                                .push().getKey();

                                        model.setMessageId(key);
                                        model.setIsNotified("no");

                                        // Save message to database
                                        database.getReference().child("Groups")
                                                .child(groupId)
                                                .child("messages")
                                                .child(key)
                                                .setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                progressDialog.dismiss();

                                                // Delete local voice file after upload
                                                java.io.File voiceFile = new java.io.File(voiceFileName);
                                                if (voiceFile.exists()) {
                                                    voiceFile.delete();
                                                }

                                                Toast.makeText(GroupInboxActivity.this, "Voice message sent successfully!", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        progressDialog.dismiss();
                                        Toast.makeText(GroupInboxActivity.this, "Failed to get sender name", Toast.LENGTH_SHORT).show();
                                    }
                                });

                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Failed to upload voice message: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (Exception e) {
            progressDialog.dismiss();
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Error during upload: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
