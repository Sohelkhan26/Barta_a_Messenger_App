package com.example.barta_a_messenger_app;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;

public class friendRequestFragment extends Fragment implements FriendRequestAdapter.FriendRequestActionListener{

    private RecyclerView recyclerView;
    private FriendRequestAdapter adapter;
    FirebaseAuth mAuth ;
    String uid;
    DatabaseReference friendRequestRef;
    private ArrayList<Request> friendRequest;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend_request, container, false);

        recyclerView=view.findViewById(R.id.recyclerView);

        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();

        friendRequest = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new FriendRequestAdapter(requireContext(), friendRequest,this);
        loadFriendRequests();
        recyclerView.setAdapter(adapter);



        return view;
    }

    private void loadFriendRequests() {
        DatabaseReference friendRequestsRef = FirebaseDatabase.getInstance().getReference("FriendRequestPending").child(uid);

        friendRequestsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                friendRequest.clear(); // Clear the list once at the beginning
                
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Request request = dataSnapshot.getValue(Request.class);
                    if (request == null || request.getSenderUid() == null) {
                        continue; // Skip invalid requests
                    }

                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("user").child(request.getSenderUid());
                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String username = snapshot.child("username").getValue(String.class);
                            String senderPhone = snapshot.child("phone").getValue(String.class);
                            
                            // Set sender's information
                            if (username != null) {
                                request.setName(username);
                            }
                            if (senderPhone != null) {
                                request.setPhone(senderPhone); // Set sender's phone number instead of receiver's
                            }
                            
                            friendRequest.add(request);
                            adapter.notifyDataSetChanged();

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("FriendRequest", "Error loading sender data: " + error.getMessage());
                        }
                    });

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
            }
        });

    }

    @Override
    public void onAcceptClicked(Request request) {
        // Retrieve the encryption key from Firebase instead of the request object
        EncryptionKeyManager keyManager = new EncryptionKeyManager(getContext());
        
        // Retrieve key from Firebase and store locally
        keyManager.retrieveAndStoreKeyFromFirebase(request.getSenderUid(), new EncryptionKeyManager.OnKeyRetrievedListener() {
            @Override
            public void onKeyRetrieved(boolean success, String encryptionKey) {
                if (success) {
                    Log.d("FriendRequest", "Successfully retrieved and stored encryption key from Firebase");
                    
                    // Continue with friend request acceptance
                    acceptFriendRequestInternal(request, keyManager);
                } else {
                    // Fallback: try to get key from request object (for backward compatibility)
                    if (request.getEncryptionKey() != null && !request.getEncryptionKey().isEmpty()) {
                        boolean keyStored = keyManager.storeEncryptionKey(request.getSenderUid(), request.getEncryptionKey());
                        if (keyStored) {
                            Log.d("FriendRequest", "Stored encryption key from request object (fallback)");
                            acceptFriendRequestInternal(request, keyManager);
                        } else {
                            Toast.makeText(getContext(), "Failed to store encryption key", Toast.LENGTH_SHORT).show();
                            keyManager.close();
                        }
                    } else {
                        Toast.makeText(getContext(), "No encryption key found for this friend request", Toast.LENGTH_SHORT).show();
                        keyManager.close();
                    }
                }
            }
        });
    }
    
    /**
     * Internal method to complete friend request acceptance after key is stored
     */
    private void acceptFriendRequestInternal(Request request, EncryptionKeyManager keyManager) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("user").child(request.getSenderUid());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String username = snapshot.child("username").getValue(String.class);
                String phone = snapshot.child("phone").getValue(String.class);
                request.setName(username);
                request.setPhone(phone);
                friendRequest.add(request);
                DatabaseReference contactsRef = FirebaseDatabase.getInstance().getReference("Contacts")
                        .child(request.getReceiverUid())
                        .child(request.getSenderUid());

                contactsRef.setValue(new Contact(request.getName(), request.getPhone(), request.getSenderUid(), "","","",new Date().getTime(),"",""));

                adapter.notifyDataSetChanged();
                friendRequest.clear();
                keyManager.close();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                keyManager.close();
            }
        });

        DatabaseReference userRef2 = FirebaseDatabase.getInstance().getReference("user").child(request.getReceiverUid());
        userRef2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String username = snapshot.child("username").getValue(String.class);
                String phone = snapshot.child("phone").getValue(String.class);
                request.setName(username);
                request.setPhone(phone);
                friendRequest.add(request);
                DatabaseReference contactsRef = FirebaseDatabase.getInstance().getReference("Contacts")
                        .child(request.getSenderUid())
                        .child(request.getReceiverUid());

                contactsRef.setValue(new Contact(request.getName(), request.getPhone(), request.getReceiverUid(), "","","",new Date().getTime(),"",""));

                adapter.notifyDataSetChanged();
                friendRequest.clear();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference friendRequestsRef = FirebaseDatabase.getInstance().getReference("FriendRequestPending")
                .child(request.getReceiverUid())
                .child(request.getSenderUid());

        friendRequestsRef.removeValue();
    }

    @Override
    public void onRejectClicked(Request request) {
        DatabaseReference friendRequestsRef = FirebaseDatabase.getInstance().getReference("FriendRequestPending")
                .child(request.getReceiverUid())
                .child(request.getSenderUid());

        friendRequestsRef.removeValue();
        adapter.notifyDataSetChanged();
        friendRequest.clear();

    }
}