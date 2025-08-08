package com.example.barta_a_messenger_app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class chatFragment extends Fragment {

    RecyclerView recyclerView;

    FirebaseAuth mAuth;

    FirebaseUser user;

    private ChatListAdapter adapter;

    private ArrayList<Contact> list;

    DatabaseReference databaseReference, userRef;

    String uid;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        FloatingActionButton fab = requireActivity().findViewById(R.id.fab_button);
        fab.setVisibility(View.VISIBLE);
        recyclerView = view.findViewById(R.id.recyclerView);

        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userRef = database.getReference("user").child(uid);

        list = new ArrayList<>();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext());
        // Remove reverse layout to show latest messages at top
        recyclerView.setLayoutManager(linearLayoutManager);

        adapter = new ChatListAdapter(requireContext(), list);
        recyclerView.setAdapter(adapter);

        Log.d("ChatFragment", "User ID: " + uid);

        databaseReference = FirebaseDatabase.getInstance().getReference("Contacts").child(uid);
        // Use orderByChild with descending order to get latest messages first
        databaseReference.orderByChild("message_time").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Contact contact = snapshot.getValue(Contact.class);
                if (contact != null) {
                    // Check if contact already exists
                    boolean exists = false;
                    for (int i = 0; i < list.size(); i++) {
                        if (list.get(i).getUid().equals(contact.getUid())) {
                            exists = true;
                            break;
                        }
                    }

                    if (!exists) {
                        // Insert contact in the right position based on message_time (latest first)
                        int insertPosition = 0;
                        for (int i = 0; i < list.size(); i++) {
                            if (list.get(i).getMessage_time() < contact.getMessage_time()) {
                                insertPosition = i;
                                break;
                            }
                            insertPosition = i + 1;
                        }

                        list.add(insertPosition, contact);
                        loadProfilePicture(contact);
                        adapter.notifyItemInserted(insertPosition);
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Contact updatedContact = snapshot.getValue(Contact.class);
                if (updatedContact != null) {
                    // Find and update the specific contact
                    for (int i = 0; i < list.size(); i++) {
                        Contact existingContact = list.get(i);
                        if (existingContact.getUid().equals(updatedContact.getUid())) {
                            // Update only the message-related fields
                            existingContact.setLast_message(updatedContact.getLast_message());
                            existingContact.setLast_sender_name(updatedContact.getLast_sender_name());
                            existingContact.setLast_message_seen(updatedContact.getLast_message_seen());
                            existingContact.setMessage_time(updatedContact.getMessage_time());

                            // Remove from current position
                            list.remove(i);

                            // Find the correct position based on message_time (latest first)
                            int newPosition = 0;
                            for (int j = 0; j < list.size(); j++) {
                                if (list.get(j).getMessage_time() < existingContact.getMessage_time()) {
                                    newPosition = j;
                                    break;
                                }
                                newPosition = j + 1;
                            }

                            // Insert at the correct position
                            list.add(newPosition, existingContact);

                            if (i != newPosition) {
                                adapter.notifyItemMoved(i, newPosition);
                            }
                            adapter.notifyItemChanged(newPosition);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Contact removedContact = snapshot.getValue(Contact.class);
                if (removedContact != null) {
                    for (int i = 0; i < list.size(); i++) {
                        if (list.get(i).getUid().equals(removedContact.getUid())) {
                            list.remove(i);
                            adapter.notifyItemRemoved(i);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // Handle if needed
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ChatFragment", "Database error: " + error.getMessage());
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AddContactActivity.class));
            }
        });

        return view;
    }

    private void loadProfilePicture(Contact contact) {
        String uid2 = contact.getUid();
        if (uid2 != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("user").child(uid2);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String profilePictureUrl = dataSnapshot.child("profilePicture").getValue(String.class);
                        contact.setProfilePic(profilePictureUrl);

                        // Find the contact in the list and update adapter
                        for (int i = 0; i < list.size(); i++) {
                            if (list.get(i).getUid().equals(contact.getUid())) {
                                adapter.notifyItemChanged(i);
                                break;
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("ChatFragment", "Error loading profile: " + databaseError.getMessage());
                }
            });
        }
    }

}
