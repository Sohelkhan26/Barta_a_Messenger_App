package com.example.barta_a_messenger_app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class GroupFragment extends Fragment {

    private RecyclerView groupRecyclerView;
    private GroupAdapter groupAdapter;
    private ArrayList<Group> groupList;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group, container, false);

        groupRecyclerView = view.findViewById(R.id.group_recycler_view);
        groupList = new ArrayList<>();
        groupAdapter = new GroupAdapter(groupList, getContext());
        
        groupRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        groupRecyclerView.setAdapter(groupAdapter);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        loadGroups();

        return view;
    }

    private void loadGroups() {
        String currentUserId = mAuth.getCurrentUser().getUid();
        
        databaseReference.child("Groups")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ArrayList<Group> newGroupList = new ArrayList<>();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Group group = dataSnapshot.getValue(Group.class);
                            if (group != null && group.getMembers().contains(currentUserId)) {
                                group.setGroupId(dataSnapshot.getKey());
                                newGroupList.add(group);
                            }
                        }
                        groupList.clear();
                        groupList.addAll(newGroupList);
                        groupAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Failed to load groups", Toast.LENGTH_SHORT).show();
                    }
                });
    }
} 