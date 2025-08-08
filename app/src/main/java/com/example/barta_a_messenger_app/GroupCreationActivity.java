package com.example.barta_a_messenger_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GroupCreationActivity extends AppCompatActivity {

    private EditText groupNameEditText;
    private Button createGroupButton;
    private RecyclerView contactsRecyclerView;
    private ContactAdapter contactAdapter;
    private ArrayList<Contact> selectedContacts = new ArrayList<>();
    private DatabaseReference databaseReference;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_creation);

        groupNameEditText = findViewById(R.id.group_name_edit_text);
        createGroupButton = findViewById(R.id.create_group_button);
        contactsRecyclerView = findViewById(R.id.contacts_recycler_view);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Set up RecyclerView
        contactsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        contactAdapter = new ContactAdapter(new ArrayList<>(), this, true);
        contactsRecyclerView.setAdapter(contactAdapter);

        // Load contacts
        loadContacts();

        createGroupButton.setOnClickListener(v -> {
            String groupName = groupNameEditText.getText().toString().trim();
            if (groupName.isEmpty()) {
                Toast.makeText(this, "Please enter a group name", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedContacts.size() < 2) {
                Toast.makeText(this, "Please select at least 2 contacts", Toast.LENGTH_SHORT).show();
                return;
            }

            createGroup(groupName);
        });
    }

    private void loadContacts() {
        databaseReference.child("Contacts").child(currentUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ArrayList<Contact> contacts = new ArrayList<>();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Contact contact = dataSnapshot.getValue(Contact.class);
                            if (contact != null) {
                                contacts.add(contact);
                            }
                        }
                        contactAdapter.updateContacts(contacts);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(GroupCreationActivity.this, "Failed to load contacts", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void createGroup(String groupName) {
        String groupId = databaseReference.child("Groups").push().getKey();
        if (groupId == null) {
            Toast.makeText(this, "Failed to create group", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create group data
        Map<String, Object> groupData = new HashMap<>();
        groupData.put("name", groupName);
        groupData.put("createdBy", currentUser.getUid());
        groupData.put("timestamp", System.currentTimeMillis());

        // Add members
        ArrayList<String> members = new ArrayList<>();
        members.add(currentUser.getUid());
        for (Contact contact : selectedContacts) {
            members.add(contact.getUid());
        }
        groupData.put("members", members);

        // Save group data
        databaseReference.child("Groups").child(groupId).setValue(groupData)
                .addOnSuccessListener(aVoid -> {
                    // Create chat for each member
                    for (String memberId : members) {
                        Map<String, Object> chatData = new HashMap<>();
                        chatData.put("groupId", groupId);
                        chatData.put("groupName", groupName);
                        chatData.put("lastMessage", "");
                        chatData.put("timestamp", System.currentTimeMillis());

                        databaseReference.child("Chats").child(memberId).child(groupId)
                                .setValue(chatData);
                    }

                    Toast.makeText(this, "Group created successfully", Toast.LENGTH_SHORT).show();
                    
                    // Navigate to home screen
                    Intent intent = new Intent(this, HomeScreen.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to create group", Toast.LENGTH_SHORT).show();
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
