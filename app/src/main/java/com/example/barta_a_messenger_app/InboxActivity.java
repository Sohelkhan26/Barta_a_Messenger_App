package com.example.barta_a_messenger_app;

import static com.google.android.gms.common.util.CollectionUtils.listOf;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;

import android.view.View;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InboxActivity extends AppCompatActivity implements ChatAdapter.OnMessageSelectListener {

    private static final int RC_AUTHORIZE_DRIVE = 10943;

    private static final int REQUEST_CODE_SIGN_IN = 1;
    private static final int REQUEST_CODE_OPEN_DOCUMENT = 2;
    private static final Log log = LogFactory.getLog(InboxActivity.class);
    TextView userName;
    Scope ACCESS_DRIVE_SCOPE = new Scope(Scopes.DRIVE_FILE);
    Scope SCOPE_EMAIL = new Scope(Scopes.EMAIL);
    private DriveServiceHelper driveServiceHelper;
    ImageView DP;
    AppCompatImageView backButton;

    RecyclerView chatRecyclerView;

    FirebaseAuth mAuth;

    AppCompatImageView sendButton;
    EditText inputMessage;

    FirebaseDatabase database;
    ImageButton imageSendButton;

    String checker = "", myUrl = "";
    Uri imagePath, fileUri;
    String imageUrl, fileUrl;

    String senderRoom, receiverRoom, senderId, receiverId;

    ArrayList<MessageModel> localMessageModel;

    private DBHelper dbHelper;
    SQLiteDatabase db;
    ValueEventListener chatListener, otherChatListener;
    ChatAdapter chatAdapter;
    String messageSenderName, senderName;

    String decryptedmessage, decryptedmessagenotification, encryptedMessage;

    ArrayList<MessageModel> selectedMessages;
    boolean isForwardMode = false;

    private Button forwardButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);

        Intent intent = getIntent();
        String fullName = intent.getStringExtra("full_name");
        String profilePictureUrl = intent.getStringExtra("profilePic");

        userName = findViewById(R.id.userName);
        String name = getIntent().getStringExtra("name");

        userName.setText(name != null ? name : (fullName != null ? fullName : "Unnamed User"));

        DP = findViewById(R.id.headImageView);

        if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
            Picasso.get().load(profilePictureUrl).into(DP);
        }

        backButton = findViewById(R.id.imageBack);
        chatRecyclerView = findViewById(R.id.chatRecyclerView);

        sendButton = findViewById(R.id.send);
        inputMessage = findViewById(R.id.inputMessage);

        imageSendButton = findViewById(R.id.image_send_button);

        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();

        senderId = mAuth.getCurrentUser().getUid();
        receiverId = getIntent().getStringExtra("uid");

        database.getReference().child("user").child(senderId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        senderName = snapshot.child("username").getValue(String.class);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        senderRoom = senderId + receiverId;
        receiverRoom = receiverId + senderId;

        dbHelper.chat_table_name = "t_" + senderRoom;

        dbHelper = new DBHelper(this);

        db = dbHelper.getWritableDatabase();

        localMessageModel = new ArrayList<>();
        localMessageModel = getAllMessages();

        chatAdapter = new ChatAdapter(localMessageModel, this, receiverId);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        chatRecyclerView.setLayoutManager(layoutManager);

        chatRecyclerView.scrollToPosition(localMessageModel.size() - 1);

        chatListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        MessageModel message = snapshot1.getValue(MessageModel.class);
                        message.setMessageId(snapshot1.getKey());
                        message.setIsNotified("yes");

                        try {
                            decryptedmessage = CryptoHelper.decrypt("H@rrY_p0tter_106", message.getMessage());
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }

                        message.setMessage(decryptedmessage);

                        localMessageModel.add(message);
                        updateLocalDatabase(message);
                        chatAdapter.notifyDataSetChanged();

                        chatRecyclerView.scrollToPosition(localMessageModel.size() - 1);
                    }

                    database.getReference().child("Contacts").child(senderId)
                            .child(receiverId).child("last_message_seen")
                            .setValue("true");

                    database.getReference().child("chats")
                            .child(senderId)
                            .child(receiverId)
                            .removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        };

        otherChatListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot datasnapshot : snapshot.getChildren()) {
                    if (!datasnapshot.getKey().equals(receiverId)) {
                        for (DataSnapshot dataSnapshot2 : datasnapshot.getChildren()) {
                            MessageModel message = dataSnapshot2.getValue(MessageModel.class);

                            if (message.getIsNotified().equals("no")) {

                                database.getReference().child("user")
                                        .child(message.getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            DataSnapshot ds = task.getResult();
                                            if (ds.exists()) {
                                                messageSenderName = ds.child("username").getValue(String.class);

                                                try {
                                                    decryptedmessagenotification = CryptoHelper.decrypt("H@rrY_p0tter_106", message.getMessage());
                                                } catch (Exception e) {
                                                    throw new RuntimeException(e);
                                                }

                                                if (message.getMessageType().equals("img")) {
                                                    decryptedmessagenotification = "sent an image";
                                                }

                                                NotificationHelper.notificationDialog(InboxActivity.this, decryptedmessagenotification, messageSenderName);

                                                database.getReference().child("chats")
                                                        .child(senderId).child(datasnapshot.getKey())
                                                        .child(dataSnapshot2.getKey())
                                                        .child("isNotified").setValue("yes");

                                            }
                                        }
                                    }
                                });

                            }
                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        database.getReference().child("chats")
                .child(senderId)
                .child(receiverId)
                .addValueEventListener(chatListener);

        database.getReference().child("chats")
                .child(senderId).addValueEventListener(otherChatListener);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = inputMessage.getText().toString();
                if (!message.isEmpty()) {

                    try {
                        encryptedMessage = CryptoHelper.encrypt("H@rrY_p0tter_106", message);

                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    MessageModel model;
                    model = new MessageModel(senderId, encryptedMessage);
                    model.setTimestamp(new Date().getTime());
                    inputMessage.setText("");

                    String key = database.getReference().child("chats")
                            .child(receiverId)
                            .child(senderId)
                            .push().getKey();

                    model.setMessageId(key);
                    model.setIsNotified("no");

                    database.getReference().child("chats")
                            .child(receiverId)
                            .child(senderId)
                            .child(key)
                            .setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            model.setMessage(message);
                            updateLocalDatabase(model);

                            localMessageModel.add(model);
                            chatAdapter.notifyDataSetChanged();
                            chatRecyclerView.scrollToPosition(localMessageModel.size() - 1);

                            database.getReference().child("Contacts").child(receiverId)
                                    .child(senderId).child("last_message")
                                    .setValue(encryptedMessage);

                            database.getReference().child("Contacts").child(receiverId)
                                    .child(senderId).child("last_sender_name")
                                    .setValue("");

                            database.getReference().child("Contacts").child(receiverId)
                                    .child(senderId).child("message_time")
                                    .setValue(model.getTimestamp());

                            database.getReference().child("Contacts").child(receiverId)
                                    .child(senderId).child("last_message_seen")
                                    .setValue("false");

                            database.getReference().child("Contacts").child(senderId)
                                    .child(receiverId).child("last_message")
                                    .setValue(encryptedMessage);

                            database.getReference().child("Contacts").child(senderId)
                                    .child(receiverId).child("last_sender_name")
                                    .setValue("You");

                            database.getReference().child("Contacts").child(senderId)
                                    .child(receiverId).child("message_time")
                                    .setValue(model.getTimestamp());

                            database.getReference().child("Contacts").child(senderId)
                                    .child(receiverId).child("last_message_seen")
                                    .setValue("true");
                        }
                    });

                }

            }
        });

        imageSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CharSequence options[] = new CharSequence[]{
                    "Images",
                    "PDF Files",
                    "MS Word Files"
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(InboxActivity.this);
                builder.setTitle("Select the File");

                builder.setItems(options, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
                            checker = "image";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent, "Select Image"), 123);
                        } else if (i == 1) {
                            checker = "pdf";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/pdf");
                            startActivityForResult(intent.createChooser(intent, "Select Pdf File"), 123);
                        } else {
                            checker = "doc";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/msword");
                            startActivityForResult(Intent.createChooser(intent, "Select Doc File"), 123);
                        }
                    }
                });
                builder.show();
            }
        });

        inputMessage.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    chatRecyclerView.scrollToPosition(localMessageModel.size() - 1);
                }
            }
        });

        chatRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                chatRecyclerView.scrollToPosition(localMessageModel.size() - 1);
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(InboxActivity.this, HomeScreen.class);

                startActivity(intent);
                finish();
            }
        });
        selectedMessages = new ArrayList<>();

        chatRecyclerView.setAdapter(chatAdapter);

        forwardButton = findViewById(R.id.forwardButton);
        forwardButton.setVisibility(View.GONE);
        chatAdapter.setOnMessageSelectListener(this);

        forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedMessages.size() > 0) {
                    // লগ করা সিলেক্টেড মেসেজগুলো
                    for (MessageModel message : selectedMessages) {
                        android.util.Log.d("SelectedMessages",
                                "Message ID: " + message.getMessageId()
                                + ", Content: " + message.getMessage()
                                + ", Type: " + message.getMessageType()
                                + ", Sender: " + message.getUid()
                                + ", Timestamp: " + message.getTimestamp()
                        );
                    }

                    showContactsDialog();
                } else {
                    Toast.makeText(InboxActivity.this, "No messages selected", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // কারেন্ট ইউজারের কন্টাক্টগুলো নিতে
        database.getReference()
                .child("Contacts")
                .child(senderId) // কারেন্ট ইউজারের UID
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // এখানে snapshot এ কারেন্ট ইউজারের সব কন্টাক্ট পাওয়া যাবে
                        for (DataSnapshot contactSnapshot : snapshot.getChildren()) {
                            String contactId = contactSnapshot.getKey(); // কন্টাক্টের UID

                            // কন্টাক্টের ডেটা নিতে
                            String lastMessage = contactSnapshot.child("last_message").getValue(String.class);
                            String lastSenderName = contactSnapshot.child("last_sender_name").getValue(String.class);
                            Long messageTime = contactSnapshot.child("message_time").getValue(Long.class);
                            String lastMessageSeen = contactSnapshot.child("last_message_seen").getValue(String.class);

                            android.util.Log.d("ContactDebug", "Contact ID: " + contactId);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        android.util.Log.e("ContactDebug", "Error: " + error.getMessage());
                    }
                });
    }

    @Override

    protected void onStop() {
        super.onStop();
        database.getReference().child("chats")
                .child(senderId)
                .child(receiverId).removeEventListener(chatListener);

        database.getReference().child("chats")
                .child(senderId).removeEventListener(otherChatListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        database.getReference().child("chats")
                .child(senderId)
                .child(receiverId).removeEventListener(chatListener);

        database.getReference().child("chats")
                .child(senderId).removeEventListener(otherChatListener);
    }

    private void updateLocalDatabase(MessageModel message) {
        ContentValues values = new ContentValues();
        values.put("MESSAGEID", message.getMessageId());
        values.put("MESSAGE", message.getMessage());
        values.put("MESSAGETYPE", message.getMessageType());
        values.put("ISNOTIFIED", message.getIsNotified());
        values.put("TIMESTAMP", message.getTimestamp());
        values.put("SENDER_ID", message.getUid());

        db.insert(dbHelper.chat_table_name, null, values);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 123 && resultCode == RESULT_OK && data != null && data.getData() != null) {

            if (checker.equals("image")) {
                imagePath = data.getData();
                uploadImageToDrive();
            } else if (checker.equals("pdf")) {
                fileUri = data.getData();
                uploadFile("pdf");
            } else if (checker.equals("doc")) {
                fileUri = data.getData();
                uploadFile("doc");
            } else {
                Toast.makeText(this, "Nothing Selected,Error", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == RC_AUTHORIZE_DRIVE) {
            // Handle the result for Google Sign-In request
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            if (task.isSuccessful()) {
                // If permission was granted, proceed with Google Drive setup
                GoogleSignInAccount account = task.getResult();
                android.util.Log.d("Inbox", "onActivityResult: " + account.getEmail());
            } else {
                // If permission was denied, show an error message or ask user to try again
                Toast.makeText(this, "Permission denied. Unable to access Google Drive", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadImageToDrive() {
        checkForGooglePermissions();
        if (driveServiceHelper == null) {
            Toast.makeText(this, "Drive Service Helper is null", Toast.LENGTH_SHORT).show();
            return;
        }
        uploadImage();
    }

    private void driveSetUp() {

        GoogleSignInAccount mAccount = GoogleSignIn.getLastSignedInAccount(this);

        GoogleAccountCredential credential
                = GoogleAccountCredential.usingOAuth2(
                        getApplicationContext(), Collections.singleton(Scopes.DRIVE_FILE));
        credential.setSelectedAccount(mAccount.getAccount());
        android.util.Log.d("Inbox ", "driveSetUp: " + mAccount.getEmail());
        Drive googleDriveService
                = new com.google.api.services.drive.Drive.Builder(
                        AndroidHttp.newCompatibleTransport(),
                        new GsonFactory(),
                        credential)
                        .setApplicationName("GoogleDriveIntegration 3")
                        .build();
        driveServiceHelper = new DriveServiceHelper(googleDriveService, this.getApplicationContext());
    }

    private void checkForGooglePermissions() {

        if (!GoogleSignIn.hasPermissions(
                GoogleSignIn.getLastSignedInAccount(getApplicationContext()),
                ACCESS_DRIVE_SCOPE,
                SCOPE_EMAIL)) {
            GoogleSignIn.requestPermissions(
                    this,
                    RC_AUTHORIZE_DRIVE,
                    GoogleSignIn.getLastSignedInAccount(getApplicationContext()),
                    ACCESS_DRIVE_SCOPE,
                    SCOPE_EMAIL);
        } else {
            driveSetUp();
        }
    }

    private void uploadImage() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading....");
        progressDialog.show();

        // Upload the image to Google Drive instead of Firebase Storage
        try {
            // Upload to Google Drive
            Task<File> uploadTask = driveServiceHelper.uploadFile(imagePath, UUID.randomUUID().toString());

            uploadTask.addOnCompleteListener(new OnCompleteListener<File>() {
                @Override
                public void onComplete(@NonNull Task<File> task) {
                    if (task.isSuccessful()) {
                        // Retrieve the file's ID and construct the download URL
                        File uploadedFile = task.getResult();
                        String fileId = uploadedFile.getId();
                        String downloadUrl = "https://drive.google.com/uc?id=" + fileId;  // Construct the download URL

                        // Encrypt the download URL before saving it in the message
                        try {
                            encryptedMessage = CryptoHelper.encrypt("H@rrY_p0tter_106", downloadUrl);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }

                        // Create a message model for the image message
                        MessageModel model = new MessageModel(senderId, encryptedMessage, "img");
                        model.setTimestamp(new Date().getTime());

                        // Push the message to the database (Firebase Realtime Database)
                        String key = database.getReference().child("chats")
                                .child(receiverId)
                                .child(senderId)
                                .push().getKey();

                        model.setMessageId(key);
                        model.setIsNotified("no");

                        // Save message to database
                        database.getReference().child("chats")
                                .child(receiverId)
                                .child(senderId)
                                .child(key)
                                .setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                model.setMessage(downloadUrl);  // Store the actual URL in the message
                                updateLocalDatabase(model);
                                localMessageModel.add(model);
                                chatAdapter.notifyDataSetChanged();
                                chatRecyclerView.scrollToPosition(localMessageModel.size() - 1);

                                // Update last message and metadata in contacts
                                database.getReference().child("Contacts").child(receiverId)
                                        .child(senderId).child("last_message")
                                        .setValue("sent an image");

                                database.getReference().child("Contacts").child(receiverId)
                                        .child(senderId).child("last_sender_name")
                                        .setValue("");

                                database.getReference().child("Contacts").child(receiverId)
                                        .child(senderId).child("message_time")
                                        .setValue(model.getTimestamp());

                                database.getReference().child("Contacts").child(receiverId)
                                        .child(senderId).child("last_message_seen")
                                        .setValue("false");

                                database.getReference().child("Contacts").child(senderId)
                                        .child(receiverId).child("last_message")
                                        .setValue("sent an image");

                                database.getReference().child("Contacts").child(senderId)
                                        .child(receiverId).child("last_sender_name")
                                        .setValue("You");

                                database.getReference().child("Contacts").child(senderId)
                                        .child(receiverId).child("message_time")
                                        .setValue(model.getTimestamp());

                                database.getReference().child("Contacts").child(senderId)
                                        .child(receiverId).child("last_message_seen")
                                        .setValue("true");

                                progressDialog.dismiss();
                            }
                        });
                    } else {
                        // Handle failure
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Failed to upload image: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (Exception e) {
            progressDialog.dismiss();
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Error during upload: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadFile(String fileType) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading....");
        progressDialog.show();
        checkForGooglePermissions();
        // Determine MIME type based on file type
        String mimeType = getMimeType(fileType);

        try {
            // Assuming fileUri is passed and refers to the file to be uploaded
            InputStream inputStream = getContentResolver().openInputStream(fileUri); // Open InputStream for the file

            // File metadata setup (name, MIME type)
            File fileMetadata = new File();
            fileMetadata.setName(getFileNameFromUri(fileUri)); // Use a method to get the file name from URI

            // Create InputStreamContent with MIME type and InputStream
            InputStreamContent mediaContent = new InputStreamContent(mimeType, inputStream);

            // Upload file to Google Drive using DriveServiceHelper
            Task<File> uploadTask = driveServiceHelper.uploadFile(fileUri, getFileNameFromUri(fileUri));

            uploadTask.addOnCompleteListener(new OnCompleteListener<File>() {
                @Override
                public void onComplete(@NonNull Task<File> task) {
                    if (task.isSuccessful()) {
                        File uploadedFile = task.getResult();
                        String fileId = uploadedFile.getId();
                        String fileUrl = "https://drive.google.com/uc?id=" + fileId; // Construct the file URL

                        // Encrypt the file URL before saving it to the database
                        try {
                            encryptedMessage = CryptoHelper.encrypt("H@rrY_p0tter_106", fileUrl);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }

                        // Create message model and save it to the database
                        MessageModel model = new MessageModel(senderId, encryptedMessage, fileType);
                        model.setTimestamp(new Date().getTime());
                        String key = database.getReference().child("chats")
                                .child(receiverId)
                                .child(senderId)
                                .push().getKey();
                        model.setMessageId(key);
                        model.setIsNotified("no");

                        // Save the message to Firebase Realtime Database
                        database.getReference().child("chats")
                                .child(receiverId)
                                .child(senderId)
                                .child(key)
                                .setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                model.setMessage(fileUrl); // Store the actual URL in the message
                                updateLocalDatabase(model);
                                localMessageModel.add(model);
                                chatAdapter.notifyDataSetChanged();
                                chatRecyclerView.scrollToPosition(localMessageModel.size() - 1);

                                // Update the contacts with the last message and timestamp
                                database.getReference().child("Contacts").child(receiverId)
                                        .child(senderId).child("last_message")
                                        .setValue("sent a file");

                                database.getReference().child("Contacts").child(receiverId)
                                        .child(senderId).child("last_sender_name")
                                        .setValue("");

                                database.getReference().child("Contacts").child(receiverId)
                                        .child(senderId).child("message_time")
                                        .setValue(model.getTimestamp());

                                database.getReference().child("Contacts").child(receiverId)
                                        .child(senderId).child("last_message_seen")
                                        .setValue("false");

                                // Repeat similar updates for the sender
                                database.getReference().child("Contacts").child(senderId)
                                        .child(receiverId).child("last_message")
                                        .setValue("sent a file");

                                database.getReference().child("Contacts").child(senderId)
                                        .child(receiverId).child("last_sender_name")
                                        .setValue("You");

                                database.getReference().child("Contacts").child(senderId)
                                        .child(receiverId).child("message_time")
                                        .setValue(model.getTimestamp());

                                database.getReference().child("Contacts").child(senderId)
                                        .child(receiverId).child("last_message_seen")
                                        .setValue("true");
                                progressDialog.dismiss();
                            }
                        });
                    } else {
                        // Handle failure to upload
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Failed to upload file: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

        } catch (IOException e) {
            progressDialog.dismiss();
            Toast.makeText(getApplicationContext(), "Error during file upload: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String getMimeType(String fileType) {
        switch (fileType) {
            case "pdf":
                return "application/pdf";
            case "doc":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document"; // MIME type for DOCX
            default:
                return "application/octet-stream"; // Default MIME type for unknown file types
        }
    }

    public ArrayList<MessageModel> getAllMessages() {
        ArrayList<MessageModel> messages = new ArrayList<>();

        db = dbHelper.getWritableDatabase();
        String createTableQuery = "CREATE TABLE IF NOT EXISTS " + dbHelper.chat_table_name
                + " (MESSAGEID TEXT PRIMARY KEY,"
                + " MESSAGE TEXT, MESSAGETYPE TEXT ,"
                + "ISNOTIFIED TEXT,"
                + "TIMESTAMP INTEGER, SENDER_ID TEXT);";

        db.execSQL(createTableQuery);

        db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                dbHelper.chat_table_name,
                null, // projection: null means all columns
                null, // selection
                null, // selectionArgs
                null, // groupBy
                null, // having
                "TIMESTAMP" + " ASC" // orderBy
        );

        while (cursor.moveToNext()) {
            MessageModel message = new MessageModel();
            message.setMessageId(cursor.getString(cursor.getColumnIndexOrThrow("MESSAGEID"))); //new added
            message.setMessage(cursor.getString(cursor.getColumnIndexOrThrow("MESSAGE")));
            message.setMessageType(cursor.getString(cursor.getColumnIndexOrThrow("MESSAGETYPE")));
            message.setIsNotified(cursor.getString(cursor.getColumnIndexOrThrow("ISNOTIFIED")));
            message.setTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow("TIMESTAMP")));
            message.setUid(cursor.getString(cursor.getColumnIndexOrThrow("SENDER_ID")));

            messages.add(message);
        }

        cursor.close();

        return messages;
    }

    private String getFileNameFromUri(Uri uri) {
        DocumentFile documentFile = DocumentFile.fromSingleUri(this, uri);
        return documentFile.getName();
    }

    private void showContactsDialog() {
        if (senderId == null) {
            android.util.Log.e("ContactsDebug", "কারেন্ট ইউজার ID খুঁজে পাওয়া যায়নি!");
            return;
        }

        // Contacts/<sender_id> থেকে কন্টাক্টগুলো নিচ্ছি
        database.getReference()
                .child("Contacts")
                .child(senderId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ArrayList<String> userNames = new ArrayList<>();
                        ArrayList<String> userIds = new ArrayList<>();

                        for (DataSnapshot contactSnapshot : snapshot.getChildren()) {
                            String contactId = contactSnapshot.getKey();

                            if (contactId != null && !contactId.equals(receiverId)) {
                                // user নোড থেকে username নিচ্ছি
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

                                                    // সব কন্টাক্ট লোড হয়ে গেলে ডায়ালগ দেখাবো
                                                    if (userNames.size() == snapshot.getChildrenCount() - 1) {
                                                        showForwardDialogWithContacts(userNames, userIds);
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                android.util.Log.e("ContactsDebug", "Error: " + error.getMessage());
                                            }
                                        });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        android.util.Log.e("ContactsDebug", "Error: " + error.getMessage());
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
                android.util.Log.d("ForwardDebug", "Selected user: " + selectedUsername + " (UID: " + selectedUID + ")");
            } else {
                android.util.Log.d("ForwardDebug", "Deselected user: " + selectedUsername + " (UID: " + selectedUID + ")");
            }
        });

        builder.setPositiveButton("Send", (dialog, which) -> {
            // সব সিলেক্টেড ইউজারদের জন্য মেসেজ ফরওয়ার্ড করা
            for (int i = 0; i < checkedItems.length; i++) {
                if (checkedItems[i]) {
                    String recipientUID = userIds.get(i);
                    forwardMessagesTo(recipientUID);
                }
            }

            // ফরওয়ার্ড বাটন হাইড করা এবং সিলেকশন ক্লিয়ার করা
            forwardButton.setVisibility(View.GONE);
            chatAdapter.clearSelection();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void forwardMessagesTo(String recipientUID) {
        if (recipientUID == null) {
            android.util.Log.e("ForwardDebug", "Recipient UID is null");
            return;
        }

        for (MessageModel message : selectedMessages) {
            try {
                android.util.Log.d("ForwardDebug", "Forwarding message to UID: " + recipientUID);

                // মেসেজ এনক্রিপ্ট করা
                String encryptedMsg = CryptoHelper.encrypt("H@rrY_p0tter_106", message.getMessage());

                // Firebase এ সেভ করার জন্য কী জেনারেট করা
                String key = database.getReference().child("chats")
                        .child(recipientUID)
                        .child(senderId)
                        .push().getKey();

                if (key == null) {
                    continue;
                }

                MessageModel forwardedMessage = new MessageModel(senderId, encryptedMsg);
                forwardedMessage.setMessageId(key);
                forwardedMessage.setTimestamp(new Date().getTime());
                forwardedMessage.setIsNotified("no");
                forwardedMessage.setMessageType(message.getMessageType());

                // রিসিপিয়েন্টের চ্যাটে মেসেজ সেভ করা (received message)
                database.getReference().child("chats")
                        .child(recipientUID)
                        .child(senderId)
                        .child(key)
                        .setValue(forwardedMessage)
                        .addOnSuccessListener(unused -> {
                            // রিসিভারের কন্টাক্টস আপডেট
                            updateContactInfo(recipientUID, senderId, encryptedMsg, "", forwardedMessage.getTimestamp(), "false");

                            // সেন্ডারের চ্যাটে একই মেসেজ সেভ করা (sent message)
                            database.getReference().child("chats")
                                    .child(senderId)
                                    .child(recipientUID)
                                    .child(key)
                                    .setValue(forwardedMessage)
                                    .addOnSuccessListener(v -> {
                                        // সেন্ডারের কন্টাক্টস আপডেট
                                        updateContactInfo(senderId, recipientUID, encryptedMsg, "You", forwardedMessage.getTimestamp(), "true");
                                    });
                        })
                        .addOnFailureListener(e -> {
                            android.util.Log.e("ForwardDebug", "Error: " + e.getMessage());
                        });

            } catch (Exception e) {
                android.util.Log.e("ForwardDebug", "Error: " + e.getMessage());
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

    @Override
    public void onMessageSelectModeActivated() {
        if (selectedMessages.size() == 0) {
            forwardButton.setVisibility(View.GONE);
            chatAdapter.clearSelection();
        }
    }

    @Override
    public void onMessageSelected(ArrayList<MessageModel> messages) {
        selectedMessages = messages;
        if (messages.size() > 0) {
            forwardButton.setVisibility(View.VISIBLE);
        } else {
            forwardButton.setVisibility(View.GONE);
            chatAdapter.clearSelection();
        }
    }

    @Override
    public void onBackPressed() {
        if (forwardButton.getVisibility() == View.VISIBLE) {
            forwardButton.setVisibility(View.GONE);
            chatAdapter.clearSelection();
        } else {
            super.onBackPressed();
        }
    }
}
