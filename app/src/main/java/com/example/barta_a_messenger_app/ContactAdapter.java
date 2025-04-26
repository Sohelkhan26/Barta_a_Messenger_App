package com.example.barta_a_messenger_app;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

    // Add interface for click listener
    public interface OnItemClickListener {
        void onItemClick(Contact contact);
    }

    private ArrayList<Contact> contacts;
    private Context context;
    private boolean isSelectionMode;
    private GroupCreationActivity groupCreationActivity;
    private OnItemClickListener listener;

    public ContactAdapter(ArrayList<Contact> contacts, Context context, boolean isSelectionMode) {
        this.contacts = contacts;
        this.context = context;
        this.isSelectionMode = isSelectionMode;
        if (context instanceof GroupCreationActivity) {
            this.groupCreationActivity = (GroupCreationActivity) context;
        }
    }

    // Add method to set click listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contact contact = contacts.get(position);
        holder.nameTextView.setText(contact.getFull_name());
        holder.phoneTextView.setText(contact.getPhone_number());

        if (isSelectionMode) {
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    groupCreationActivity.onContactSelected(contact);
                } else {
                    groupCreationActivity.onContactDeselected(contact);
                }
            });
        } else {
            holder.checkBox.setVisibility(View.GONE);
        }

        // Load profile picture using Picasso
        if (contact.getProfilePic() != null && !contact.getProfilePic().isEmpty()) {
            Picasso.get().load(contact.getProfilePic())
                    .placeholder(R.drawable.default_profile)
                    .into(holder.profile);
        } else {
            holder.profile.setImageResource(R.drawable.default_profile);
        }

        // Set click listener on item view
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(contact);
            }
        });
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public void updateContacts(ArrayList<Contact> newContacts) {
        this.contacts = newContacts;
        notifyDataSetChanged();
    }

    static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView phoneTextView;
        CheckBox checkBox;
        ImageView profile;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.contact_name);
            phoneTextView = itemView.findViewById(R.id.contact_phone);
            checkBox = itemView.findViewById(R.id.contact_checkbox);
            profile = itemView.findViewById(R.id.contact_profile);
        }
    }
}
