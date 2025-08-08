package com.example.barta_a_messenger_app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ForwardContactAdapter extends RecyclerView.Adapter<ForwardContactAdapter.ViewHolder> {

    private ArrayList<ForwardContact> contacts;
    private OnContactSelectListener listener;

    public interface OnContactSelectListener {
        void onContactSelected(ForwardContact contact);
        void onContactDeselected(ForwardContact contact);
    }

    public ForwardContactAdapter(ArrayList<ForwardContact> contacts, OnContactSelectListener listener) {
        this.contacts = contacts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_forward_contact, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ForwardContact contact = contacts.get(position);
        
        holder.nameTextView.setText(contact.getName());
        holder.statusTextView.setText(contact.getStatus());
        
        // Load profile picture
        if (contact.getProfilePic() != null && !contact.getProfilePic().isEmpty()) {
            Picasso.get()
                    .load(contact.getProfilePic())
                    .placeholder(R.drawable.profile_icon)
                    .error(R.drawable.profile_icon)
                    .into(holder.profileImageView);
        } else {
            holder.profileImageView.setImageResource(R.drawable.profile_icon);
        }

        // Set checkbox state
        holder.checkBox.setOnCheckedChangeListener(null); // Clear listener to avoid unwanted triggers
        holder.checkBox.setChecked(contact.isSelected());
        
        // Set up click listeners
        View.OnClickListener clickListener = v -> {
            boolean newState = !contact.isSelected();
            contact.setSelected(newState);
            holder.checkBox.setChecked(newState);
            
            if (newState) {
                listener.onContactSelected(contact);
            } else {
                listener.onContactDeselected(contact);
            }
        };

        holder.itemView.setOnClickListener(clickListener);
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked != contact.isSelected()) {
                contact.setSelected(isChecked);
                if (isChecked) {
                    listener.onContactSelected(contact);
                } else {
                    listener.onContactDeselected(contact);
                }
            }
        });

        // Add selection visual feedback
        if (contact.isSelected()) {
            holder.itemView.setBackgroundResource(R.drawable.selected_contact_background);
        } else {
            holder.itemView.setBackgroundResource(R.drawable.contact_background);
        }
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImageView;
        TextView nameTextView;
        TextView statusTextView;
        CheckBox checkBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.profileImage);
            nameTextView = itemView.findViewById(R.id.contactName);
            statusTextView = itemView.findViewById(R.id.contactStatus);
            checkBox = itemView.findViewById(R.id.contactCheckBox);
        }
    }
}
