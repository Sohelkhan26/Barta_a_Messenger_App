package com.example.barta_a_messenger_app;

<<<<<<< HEAD
=======

>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c
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

import android.widget.EditText;
import android.widget.ImageButton;

import android.widget.ImageView;
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
import java.util.UUID;

public class InboxActivity extends AppCompatActivity {

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

<<<<<<< HEAD
    String checker = "", myUrl = "";
    Uri imagePath, fileUri;
    String imageUrl, fileUrl;

    String senderRoom, receiverRoom, senderId, receiverId;
=======
    String checker="",myUrl="";
    Uri imagePath,fileUri;
    String imageUrl,fileUrl;

    String senderRoom,receiverRoom,senderId,receiverId;
>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c

    ArrayList<MessageModel> localMessageModel;

    private DBHelper dbHelper;
    SQLiteDatabase db;
<<<<<<< HEAD
    ValueEventListener chatListener, otherChatListener;
    ChatAdapter chatAdapter;
    String messageSenderName, senderName;

    String decryptedmessage, decryptedmessagenotification, encryptedMessage;
=======
    ValueEventListener chatListener,otherChatListener;
    ChatAdapter chatAdapter;
    String messageSenderName,senderName;

    String decryptedmessage,decryptedmessagenotification,encryptedMessage;
>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);

        userName = findViewById(R.id.userName);
<<<<<<< HEAD
        String name = getIntent().getStringExtra("name");
        userName.setText(name != null ? name : "User");

        DP = findViewById(R.id.headImageView);
        String profilePictureUrl = getIntent().getStringExtra("profilePic");
=======
        userName.setText(getIntent().getStringExtra("Name").toString());
         DP = findViewById(R.id.headImageView);

         String profilePictureUrl = getIntent().getStringExtra("profile_pic").toString();
>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c

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

<<<<<<< HEAD
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
=======


        senderId = mAuth.getCurrentUser().getUid();
        receiverId = getIntent().getStringExtra("contact_uid").toString();

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
>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c

        senderRoom = senderId + receiverId;
        receiverRoom = receiverId + senderId;

<<<<<<< HEAD
        dbHelper.chat_table_name = "t_" + senderRoom;

        dbHelper = new DBHelper(this);

=======
        dbHelper.chat_table_name="t_"+senderRoom;

        dbHelper = new DBHelper(this);


>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c
        db = dbHelper.getWritableDatabase();

        localMessageModel = new ArrayList<>();
        localMessageModel = getAllMessages();
<<<<<<< HEAD

        chatAdapter = new ChatAdapter(localMessageModel, this, receiverId);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        chatRecyclerView.setLayoutManager(layoutManager);

        chatRecyclerView.scrollToPosition(localMessageModel.size() - 1);
=======
//        for(int i=0;i<localMessageModel.size();i++){
//            Log.d("message",localMessageModel.get(i).getMessage());
//        }



        chatAdapter = new ChatAdapter(localMessageModel,this,receiverId);


        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        chatRecyclerView.setLayoutManager(layoutManager);

        chatRecyclerView.scrollToPosition(localMessageModel.size()-1);

>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c

        chatListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
<<<<<<< HEAD
                if (snapshot.exists()) {
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
=======
                if(snapshot.exists()){
                    //localMessageModel.clear();
                    for(DataSnapshot snapshot1:snapshot.getChildren()){
>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c
                        MessageModel message = snapshot1.getValue(MessageModel.class);
                        message.setMessageId(snapshot1.getKey());
                        message.setIsNotified("yes");

<<<<<<< HEAD
                        try {
                            decryptedmessage = CryptoHelper.decrypt("H@rrY_p0tter_106", message.getMessage());
=======
                        try{
                            decryptedmessage = CryptoHelper.decrypt("H@rrY_p0tter_106",message.getMessage());
>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }

                        message.setMessage(decryptedmessage);

                        localMessageModel.add(message);
                        updateLocalDatabase(message);
                        chatAdapter.notifyDataSetChanged();

<<<<<<< HEAD
                        chatRecyclerView.scrollToPosition(localMessageModel.size() - 1);
                    }

=======
                        chatRecyclerView.scrollToPosition(localMessageModel.size()-1);
                    }


//                    database.getReference().child("Contacts").child(senderId)
//                            .child(receiverId).child("last_message")
//                            .setValue(localMessageModel.get(localMessageModel.size()-1).getMessage());
//
//                    database.getReference().child("Contacts").child(senderId)
//                            .child(receiverId).child("last_sender_name")
//                                    .setValue(getIntent().getStringExtra("Name").toString());
//
//                    database.getReference().child("Contacts").child(senderId)
//                            .child(receiverId).child("message_time")
//                                    .setValue(localMessageModel.get(localMessageModel.size()-1).getTimestamp());

>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c
                    database.getReference().child("Contacts").child(senderId)
                            .child(receiverId).child("last_message_seen")
                            .setValue("true");

                    database.getReference().child("chats").child(senderId).child(receiverId).removeValue();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        otherChatListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot datasnapshot : snapshot.getChildren()) {
<<<<<<< HEAD
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
=======
                    if(!datasnapshot.getKey().equals(receiverId)){
                        for(DataSnapshot dataSnapshot2 : datasnapshot.getChildren()){
                            MessageModel message = dataSnapshot2.getValue(MessageModel.class);

                            if(message.getIsNotified().equals("no")){

                                database.getReference().child("user")
                                        .child(message.getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                                if(task.isSuccessful()){
                                                    DataSnapshot ds = task.getResult();
                                                    if(ds.exists()){
                                                        messageSenderName = ds.child("username").getValue(String.class);

                                                        try{
                                                            decryptedmessagenotification = CryptoHelper.decrypt("H@rrY_p0tter_106",message.getMessage());
                                                        } catch (Exception e) {
                                                            throw new RuntimeException(e);
                                                        }

                                                        if(message.getMessageType().equals("img")){
                                                            decryptedmessagenotification = "sent an image";
                                                        }

                                                        NotificationHelper.notificationDialog(InboxActivity.this,decryptedmessagenotification,messageSenderName);

                                                        database.getReference().child("chats")
                                                                .child(senderId).child(datasnapshot.getKey())
                                                                .child(dataSnapshot2.getKey())
                                                                .child("isNotified").setValue("yes");


//                                                        database.getReference().child("Contacts").child(senderId)
//                                                                .child(datasnapshot.getKey()).child("last_message")
//                                                                .setValue(message.getMessage());
//
//                                                        database.getReference().child("Contacts").child(senderId)
//                                                                .child(datasnapshot.getKey()).child("last_sender_name")
//                                                                .setValue(message.getUid());
//
//                                                        database.getReference().child("Contacts").child(senderId)
//                                                                .child(datasnapshot.getKey()).child("message_time")
//                                                                .setValue(message.getTimestamp());

                                                    }
                                                }
                                            }
                                        });




>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c

                            }
                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

<<<<<<< HEAD
        database.getReference().child("chats")
                .child(senderId)
                .child(receiverId)
                .addValueEventListener(chatListener);
=======
                database.getReference().child("chats")
                        .child(senderId)
                        .child(receiverId)
                                .addValueEventListener(chatListener);
>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c

        database.getReference().child("chats")
                .child(senderId).addValueEventListener(otherChatListener);

<<<<<<< HEAD
=======


>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = inputMessage.getText().toString();
<<<<<<< HEAD
                if (!message.isEmpty()) {

                    try {
                        encryptedMessage = CryptoHelper.encrypt("H@rrY_p0tter_106", message);
=======
                if(!message.isEmpty()){

                    try {
                        encryptedMessage = CryptoHelper.encrypt("H@rrY_p0tter_106",message);
>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c

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
<<<<<<< HEAD
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
=======
                                @Override
                                public void onSuccess(Void unused) {
                                    model.setMessage(message);
                                    updateLocalDatabase(model);

                                    localMessageModel.add(model);
                                    chatAdapter.notifyDataSetChanged();
                                    chatRecyclerView.scrollToPosition(localMessageModel.size()-1);

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
>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c

                }

            }
        });

        imageSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
<<<<<<< HEAD
                CharSequence options[] = new CharSequence[]{
                    "Images",
                    "PDF Files",
                    "MS Word Files"
                };
=======
                CharSequence options[] = new CharSequence[]
                        {
                                "Images",
                                "PDF Files",
                                "MS Word Files"
                        };
>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c

                AlertDialog.Builder builder = new AlertDialog.Builder(InboxActivity.this);
                builder.setTitle("Select the File");

<<<<<<< HEAD
                builder.setItems(options, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
                            checker = "image";
=======
                builder.setItems(options, new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(i==0){
                            checker="image";
>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
<<<<<<< HEAD
                            startActivityForResult(intent.createChooser(intent, "Select Image"), 123);
                        } else if (i == 1) {
                            checker = "pdf";
=======
                            startActivityForResult(intent.createChooser(intent,"Select Image"),123);
                        }
                        else if(i==1){
                            checker="pdf";
>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/pdf");
<<<<<<< HEAD
                            startActivityForResult(intent.createChooser(intent, "Select Pdf File"), 123);
                        } else {
                            checker = "doc";
=======
                            startActivityForResult(intent.createChooser(intent,"Select Pdf File"),123);
                        }
                        else{
                            checker="doc";
>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/msword");
<<<<<<< HEAD
                            startActivityForResult(Intent.createChooser(intent, "Select Doc File"), 123);
=======
                            startActivityForResult(Intent.createChooser(intent,"Select Doc File"),123);
>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c
                        }
                    }
                });
                builder.show();
            }
        });

<<<<<<< HEAD
        inputMessage.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    chatRecyclerView.scrollToPosition(localMessageModel.size() - 1);
=======

        inputMessage.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(hasFocus){
                    chatRecyclerView.scrollToPosition(localMessageModel.size()-1);
>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c
                }
            }
        });

        chatRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
<<<<<<< HEAD
                chatRecyclerView.scrollToPosition(localMessageModel.size() - 1);
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(InboxActivity.this, HomeScreen.class);
=======
                chatRecyclerView.scrollToPosition(localMessageModel.size()-1);
                // when the edittext is pressed , the onscreen keyboard is displayed. so the recyclerview is hidden.
                // setonclicklistener will also not work for first click.
                // cause after first click the recylerview will go to the botton position , that's right,
                // but then the on screen keyboard will pop up
                // and the recylerview's bottom portion will be hidden again.
                //so i've used the scrollToPosition method when the on screen keyboard appears

            }
        });


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(InboxActivity.this,HomeScreen.class);
>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c

                startActivity(intent);
                finish();
            }
        });

        chatRecyclerView.setAdapter(chatAdapter);
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

<<<<<<< HEAD
=======
//    @Override
//    protected void onPause() {
//        super.onPause();
//        database.getReference().child("chats")
//                .child(senderId)
//                .child(receiverRoom).removeEventListener(chatListener);
//    }

>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c
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
<<<<<<< HEAD
        values.put("MESSAGETYPE", message.getMessageType());
        values.put("ISNOTIFIED", message.getIsNotified());
        values.put("TIMESTAMP", message.getTimestamp());
        values.put("SENDER_ID", message.getUid());
=======
        values.put("MESSAGETYPE",message.getMessageType());
        values.put("ISNOTIFIED",message.getIsNotified());
        values.put("TIMESTAMP", message.getTimestamp());
        values.put("SENDER_ID",message.getUid());
>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c

        db.insert(dbHelper.chat_table_name, null, values);

    }

<<<<<<< HEAD
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
=======

    @Override
    protected void onActivityResult(int requestCode,int resultCode,@Nullable Intent data){
        super.onActivityResult(requestCode,resultCode,data);

        if(requestCode==123 && resultCode==RESULT_OK && data!=null && data.getData()!=null){

            if(checker.equals("image")){
                imagePath = data.getData();
                uploadImageToDrive();
            }
            else if(checker.equals("pdf")){
                fileUri = data.getData();
                uploadFile("pdf");
            }
            else if(checker.equals("doc")){
                fileUri = data.getData();
                uploadFile("doc");
            }
            else{
                Toast.makeText(this,"Nothing Selected,Error",Toast.LENGTH_SHORT).show();
            }
        }
        if(requestCode == RC_AUTHORIZE_DRIVE){
>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c
            // Handle the result for Google Sign-In request
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            if (task.isSuccessful()) {
                // If permission was granted, proceed with Google Drive setup
                GoogleSignInAccount account = task.getResult();
                android.util.Log.d("Inbox", "onActivityResult: " + account.getEmail());
<<<<<<< HEAD
=======
//                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c
            } else {
                // If permission was denied, show an error message or ask user to try again
                Toast.makeText(this, "Permission denied. Unable to access Google Drive", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadImageToDrive() {
        checkForGooglePermissions();
<<<<<<< HEAD
        if (driveServiceHelper == null) {
=======
        if(driveServiceHelper == null){
>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c
            Toast.makeText(this, "Drive Service Helper is null", Toast.LENGTH_SHORT).show();
            return;
        }
        uploadImage();
<<<<<<< HEAD
=======
        // Create folder asynchronously
//        Task<GoogleDriveFileHolder> folderCreationTask = driveServiceHelper.createFolder("sohel", null);
//
//        folderCreationTask.addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                GoogleDriveFileHolder result = task.getResult();
//                Toast.makeText(this, "Folder created with ID: " + result.getId(), Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(this, "Error creating folder: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c
    }

    private void driveSetUp() {

        GoogleSignInAccount mAccount = GoogleSignIn.getLastSignedInAccount(this);

<<<<<<< HEAD
        GoogleAccountCredential credential
                = GoogleAccountCredential.usingOAuth2(
                        getApplicationContext(), Collections.singleton(Scopes.DRIVE_FILE));
        credential.setSelectedAccount(mAccount.getAccount());
        android.util.Log.d("Inbox ", "driveSetUp: " + mAccount.getEmail());
        Drive googleDriveService
                = new com.google.api.services.drive.Drive.Builder(
=======
        GoogleAccountCredential credential =
                GoogleAccountCredential.usingOAuth2(
                        getApplicationContext(), Collections.singleton(Scopes.DRIVE_FILE));
        credential.setSelectedAccount(mAccount.getAccount());
        android.util.Log.d("Inbox ", "driveSetUp: " + mAccount.getEmail());
//        Toast.makeText(this, "Inside Drive setup", Toast.LENGTH_SHORT).show();
        Drive googleDriveService =
                new com.google.api.services.drive.Drive.Builder(
>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c
                        AndroidHttp.newCompatibleTransport(),
                        new GsonFactory(),
                        credential)
                        .setApplicationName("GoogleDriveIntegration 3")
                        .build();
<<<<<<< HEAD
        driveServiceHelper = new DriveServiceHelper(googleDriveService, this.getApplicationContext());
=======
        driveServiceHelper = new DriveServiceHelper(googleDriveService , this.getApplicationContext());
>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c
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
<<<<<<< HEAD
        }
    }

=======
//            Toast.makeText(this, "Permission to access Drive and Email has been granted", Toast.LENGTH_SHORT).show();
        }
    }


//    private void uploadImage() {
//        ProgressDialog progressDialog = new ProgressDialog(this);
//        progressDialog.setTitle("Uploading....");
//        progressDialog.show();
//
//        FirebaseStorage.getInstance().getReference("chat_images/"+ UUID.randomUUID().toString()).putFile(imagePath).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
//                if (task.isSuccessful()){
//                    task.getResult().getStorage().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
//                        @Override
//                        public void onComplete(@NonNull Task<Uri> task) {
//                            if (task.isSuccessful()){
//                                imageUrl = task.getResult().toString();
//
//                                try{
//                                    encryptedMessage = CryptoHelper.encrypt("H@rrY_p0tter_106",imageUrl);
//                                }
//                                catch (Exception e) {
//                                    throw new RuntimeException(e);
//                                }
//
//                                MessageModel model = new MessageModel(senderId,encryptedMessage,"img");
//                                model.setTimestamp(new Date().getTime());
//
//
//                                String key = database.getReference().child("chats")
//                                        .child(receiverId)
//                                        .child(senderId)
//                                        .push().getKey();
//
//                                model.setMessageId(key);
//                                model.setIsNotified("no");
//
//                                database.getReference().child("chats")
//                                        .child(receiverId)
//                                        .child(senderId)
//                                        .child(key)
//                                        .setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
//                                            @Override
//                                            public void onSuccess(Void unused) {
//                                                model.setMessage(imageUrl);
//                                                updateLocalDatabase(model);
//                                                localMessageModel.add(model);
//                                                chatAdapter.notifyDataSetChanged();
//                                                chatRecyclerView.scrollToPosition(localMessageModel.size()-1);
//
//                                                database.getReference().child("Contacts").child(receiverId)
//                                                        .child(senderId).child("last_message")
//                                                        .setValue("sent an image");
//
//                                                database.getReference().child("Contacts").child(receiverId)
//                                                        .child(senderId).child("last_sender_name")
//                                                        .setValue("");
//
//                                                database.getReference().child("Contacts").child(receiverId)
//                                                        .child(senderId).child("message_time")
//                                                        .setValue(model.getTimestamp());
//
//                                                database.getReference().child("Contacts").child(receiverId)
//                                                        .child(senderId).child("last_message_seen")
//                                                        .setValue("false");
//
//                                                database.getReference().child("Contacts").child(senderId)
//                                                        .child(receiverId).child("last_message")
//                                                        .setValue("sent an image");
//
//                                                database.getReference().child("Contacts").child(senderId)
//                                                        .child(receiverId).child("last_sender_name")
//                                                        .setValue("You");
//
//                                                database.getReference().child("Contacts").child(senderId)
//                                                        .child(receiverId).child("message_time")
//                                                        .setValue(model.getTimestamp());
//
//                                                database.getReference().child("Contacts").child(senderId)
//                                                        .child(receiverId).child("last_message_seen")
//                                                        .setValue("true");
//
//                                                progressDialog.dismiss();
//                                            }
//                                        });
//
////                                updateProfilePicture(task.getResult().toString());
//                            }
//
//                        }
//
//                    });
////                    Toast.makeText(ProfileActivity.this, "Image Uploaded", Toast.LENGTH_SHORT).show();
//                }
//                else {
////                    Toast.makeText(ProfileActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
//                }
//
//            }
//
//        });
//
//
//    }

>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c
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
<<<<<<< HEAD
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
=======
                                    @Override
                                    public void onSuccess(Void unused) {
                                        model.setMessage(downloadUrl);  // Store the actual URL in the message
                                        updateLocalDatabase(model);
                                        localMessageModel.add(model);
                                        chatAdapter.notifyDataSetChanged();
                                        chatRecyclerView.scrollToPosition(localMessageModel.size() - 1);

                                        // Update last message and metadata in contacts
//                                        updateContacts(model);
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
>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c
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

<<<<<<< HEAD
=======

>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c
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
<<<<<<< HEAD
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
=======
                                    @Override
                                    public void onSuccess(Void unused) {
                                        model.setMessage(fileUrl); // Store the actual URL in the message
                                        updateLocalDatabase(model);
                                        localMessageModel.add(model);
                                        chatAdapter.notifyDataSetChanged();
                                        chatRecyclerView.scrollToPosition(localMessageModel.size() - 1);

                                        // Update the contacts with the last message and timestamp
//                                        updateContacts(model);
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
>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c
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

<<<<<<< HEAD
=======

>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c
    public ArrayList<MessageModel> getAllMessages() {
        ArrayList<MessageModel> messages = new ArrayList<>();

        db = dbHelper.getWritableDatabase();
<<<<<<< HEAD
        String createTableQuery = "CREATE TABLE IF NOT EXISTS " + dbHelper.chat_table_name
                + " (MESSAGEID TEXT PRIMARY KEY,"
                + " MESSAGE TEXT, MESSAGETYPE TEXT ,"
                + "ISNOTIFIED TEXT,"
                + "TIMESTAMP INTEGER, SENDER_ID TEXT);";
=======
        String createTableQuery = "CREATE TABLE IF NOT EXISTS "+dbHelper.chat_table_name+
                " (MESSAGEID TEXT PRIMARY KEY," +
                " MESSAGE TEXT, MESSAGETYPE TEXT ," +
                "ISNOTIFIED TEXT,"+
                "TIMESTAMP INTEGER, SENDER_ID TEXT);";
>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c

        db.execSQL(createTableQuery);

        db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                dbHelper.chat_table_name,
<<<<<<< HEAD
                null, // projection: null means all columns
                null, // selection
                null, // selectionArgs
                null, // groupBy
                null, // having
                "TIMESTAMP" + " ASC" // orderBy
=======
                null,  // projection: null means all columns
                null,  // selection
                null,  // selectionArgs
                null,  // groupBy
                null,  // having
                "TIMESTAMP" + " ASC"  // orderBy
>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c
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

<<<<<<< HEAD
=======
        //db.close();

        //Log.d("message",messages.toString());

>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c
        return messages;
    }

    private String getFileNameFromUri(Uri uri) {
        DocumentFile documentFile = DocumentFile.fromSingleUri(this, uri);
        return documentFile.getName();
    }
<<<<<<< HEAD
}
=======
}
>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c
