package com.example.barta_a_messenger_app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

<<<<<<< HEAD
import android.util.Log;
=======
>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class chatFragment extends Fragment {

    RecyclerView recyclerView;

<<<<<<< HEAD
    FirebaseAuth mAuth;
=======
    FirebaseAuth mAuth ;
>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c
    FirebaseUser user;

    private ChatListAdapter adapter;

    private ArrayList<Contact> list;

<<<<<<< HEAD
    DatabaseReference databaseReference, userRef;
=======
    DatabaseReference databaseReference,userRef;
>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c
    String uid;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

<<<<<<< HEAD
        FloatingActionButton fab = requireActivity().findViewById(R.id.fab_button);
        fab.setVisibility(View.VISIBLE);
        recyclerView = view.findViewById(R.id.recyclerView);
=======
        FloatingActionButton fab =requireActivity().findViewById(R.id.fab_button);
        fab.setVisibility(View.VISIBLE);
        recyclerView=view.findViewById(R.id.recyclerView);
>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c

        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userRef = database.getReference("user").child(uid);

        list = new ArrayList<>();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        adapter = new ChatListAdapter(requireContext(), list);
        recyclerView.setAdapter(adapter);

<<<<<<< HEAD
        Log.d("ChatFragment", "User ID: " + uid);
=======

>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c
        databaseReference = FirebaseDatabase.getInstance().getReference("Contacts").child(uid);
        databaseReference.orderByChild("message_time").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Contact contact = dataSnapshot.getValue(Contact.class);
                    list.add(contact);

                    String uid2 = contact.getUid();
<<<<<<< HEAD
                    if (uid2 != null) {
=======
                    if (uid2 != null){
>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c
                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("user").child(uid2);

                        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    String profilePictureUrl = dataSnapshot.child("profilePicture").getValue(String.class);
                                    contact.setProfilePic(profilePictureUrl);
<<<<<<< HEAD
                                    adapter.notifyDataSetChanged();
=======
                                    adapter.notifyDataSetChanged();  // Notify adapter to update the UI
>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                // Handle errors
                            }
                        });
                    }
<<<<<<< HEAD
                }
                adapter.notifyDataSetChanged();
            }

=======

                }
                adapter.notifyDataSetChanged();
            }
>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle errors
            }
        });

<<<<<<< HEAD
=======

>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AddContactActivity.class));
            }
        });

<<<<<<< HEAD
        return view;
    }

}
=======

        return view;
    }



}
>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c
