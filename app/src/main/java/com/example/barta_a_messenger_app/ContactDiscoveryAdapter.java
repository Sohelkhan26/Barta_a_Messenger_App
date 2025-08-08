package com.example.barta_a_messenger_app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class ContactDiscoveryAdapter extends RecyclerView.Adapter<ContactDiscoveryAdapter.ViewHolder> {
    private List<DiscoveredContact> contacts = new ArrayList<>();
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;

    public ContactDiscoveryAdapter() {
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
    }

    public void setContacts(List<DiscoveredContact> contacts) {
        this.contacts = contacts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_discovered_contact, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DiscoveredContact contact = contacts.get(position);
        holder.tvName.setText(contact.getName());
        holder.tvPhone.setText(contact.getPhoneNumber());
        
        if (contact.isAdded()) {
            holder.btnAdd.setText("Added");
            holder.btnAdd.setEnabled(false);
        } else {
            holder.btnAdd.setText("Add");
            holder.btnAdd.setEnabled(true);
            holder.btnAdd.setOnClickListener(v -> addContact(contact, holder));
        }
    }

    private void addContact(DiscoveredContact contact, ViewHolder holder) {
        String currentUserId = mAuth.getCurrentUser().getUid();
        
        // Get the other user's UID from their phone number
        database.getReference("All Accounts")
                .orderByChild("phone_no")
                .equalTo(contact.getPhoneNumber())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                String otherUserId = userSnapshot.child("uid").getValue(String.class);
                                if (otherUserId != null) {
                                    // Add contact for current user
                                    DatabaseReference currentUserContactRef = database.getReference("Contacts")
                                            .child(currentUserId)
                                            .child(otherUserId);

                                    Contact newContact = new Contact(
                                            contact.getName(),
                                            contact.getPhoneNumber(),
                                            otherUserId,
                                            "", // last_message
                                            "", // last_sender_name
                                            "", // last_message_seen
                                            System.currentTimeMillis(), // message_time
                                            "", // status
                                            "" // profilePic
                                    );
                                    
                                    currentUserContactRef.setValue(newContact)
                                            .addOnSuccessListener(aVoid -> {
                                                // Add contact for other user
                                                database.getReference("user")
                                                        .child(currentUserId)
                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot currentUserSnapshot) {
                                                                String currentUserName = currentUserSnapshot.child("username").getValue(String.class);
                                                                String currentUserPhone = currentUserSnapshot.child("phone").getValue(String.class);
                                                                
                                                                if (currentUserName != null && currentUserPhone != null) {
                                                                    Contact reverseContact = new Contact(
                                                                            currentUserName,
                                                                            currentUserPhone,
                                                                            currentUserId,
                                                                            "", // last_message
                                                                            "", // last_sender_name
                                                                            "", // last_message_seen
                                                                            System.currentTimeMillis(), // message_time
                                                                            "", // status
                                                                            "" // profilePic
                                                                    );
                                                                    
                                                                    database.getReference("Contacts")
                                                                            .child(otherUserId)
                                                                            .child(currentUserId)
                                                                            .setValue(reverseContact)
                                                                            .addOnSuccessListener(aVoid2 -> {
                                                                                contact.setAdded(true);
                                                                                holder.btnAdd.setText("Added");
                                                                                holder.btnAdd.setEnabled(false);
                                                                                Toast.makeText(holder.itemView.getContext(),
                                                                                        "Contact added successfully",
                                                                                        Toast.LENGTH_SHORT).show();
                                                                            })
                                                                            .addOnFailureListener(e ->
                                                                                    Toast.makeText(holder.itemView.getContext(),
                                                                                            "Failed to add contact: " + e.getMessage(),
                                                                                            Toast.LENGTH_SHORT).show());
                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError error) {
                                                                Toast.makeText(holder.itemView.getContext(),
                                                                        "Failed to add contact: " + error.getMessage(),
                                                                        Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                            })
                                            .addOnFailureListener(e ->
                                                    Toast.makeText(holder.itemView.getContext(),
                                                            "Failed to add contact: " + e.getMessage(),
                                                            Toast.LENGTH_SHORT).show());
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(holder.itemView.getContext(),
                                "Failed to add contact: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvPhone;
        Button btnAdd;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_contact_name);
            tvPhone = itemView.findViewById(R.id.tv_contact_phone);
            btnAdd = itemView.findViewById(R.id.btn_add_contact);
        }
    }
}
