package com.example.barta_a_messenger_app;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
                friendRequest.clear(); // Clear the list outside the loop to avoid repeated clearing
                Log.d(TAG, "Number of friend requests found: " + snapshot.getChildrenCount());

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Request request = dataSnapshot.getValue(Request.class);

                    if (request != null) {
                        // Log all Request object fields
                        Log.d(TAG, "Request details:");
                        Log.d(TAG, "- SenderUid: " + request.getSenderUid());
                        Log.d(TAG, "- ReceiverUid: " + request.getReceiverUid());
                        Log.d(TAG, "- Phone: " + request.getPhone());
                        Log.d(TAG, "- Name: " + request.getName());
                        Log.d(TAG, "- EncryptionKey: " + (request.getEncryptionKey() != null ? "present" : "missing"));
                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("user").child(request.getSenderUid());
                        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String username = snapshot.child("username").getValue(String.class);
                                request.setName(username);
                                friendRequest.add(request); // Add the request to the list
                                Log.d(TAG, "Retrieved username: " + username + " for user: " + request.getSenderUid());
                                adapter.notifyDataSetChanged(); // Notify adapter after adding
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                // Handle errors
                            }
                        });
                    }
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

        //Retrieve and hash the recipient's phone number
        SharedPreferences preferences = getContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String myPhoneNumber = preferences.getString("phone_number", "");
        String myName = preferences.getString("username", "");
        String myHashedPhoneNumber = CryptoHelper.hash(myPhoneNumber);

        //Validate the hashed phone number from the request
        if (!myHashedPhoneNumber.equals(request.getPhone())) {
            Toast.makeText(getContext(), "Verification failed!", Toast.LENGTH_SHORT).show();
            return;
        }

        EncryptionDB encryptionDB = new EncryptionDB(this.getContext());
        encryptionDB.insert(request.getSenderUid() , request.getEncryptionKey());



        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("user").child(request.getSenderUid());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //Retrieve sender details
                String SenderUsername = snapshot.child("username").getValue(String.class);
                String SenderPhone = snapshot.child("phone").getValue(String.class);

                // Add sender to receiver's contacts
                DatabaseReference receiverContactsRef = FirebaseDatabase.getInstance().getReference("Contacts")
                        .child(request.getReceiverUid())
                        .child(request.getSenderUid());

                receiverContactsRef.setValue(new Contact(SenderUsername, SenderPhone, request.getSenderUid(), "","","",new Date().getTime(),"",""));


                // Add receiver to sender's contacts (use the retrieved "myName")
                DatabaseReference senderContactsRef = FirebaseDatabase.getInstance().getReference("Contacts")
                        .child(request.getSenderUid())
                        .child(request.getReceiverUid());
                senderContactsRef.setValue(new Contact(myName, myPhoneNumber, request.getReceiverUid(),
                        "", "", "", new Date().getTime(), "", ""));

                // Notify user and clean up
                Toast.makeText(getContext(), "Friend request accepted!", Toast.LENGTH_SHORT).show();
                adapter.notifyDataSetChanged();
                friendRequest.clear();            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Remove the friend request from Firebase
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