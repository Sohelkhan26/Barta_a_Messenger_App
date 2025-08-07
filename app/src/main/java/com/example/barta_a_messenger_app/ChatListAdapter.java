package com.example.barta_a_messenger_app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.MyViewHolder> {

    Context context;
    static ArrayList<Contact> list;
    String decryptedmessage;

    public ChatListAdapter(Context context, ArrayList<Contact> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(context).inflate(R.layout.contacts, parent, false);
        return new ChatListAdapter.MyViewHolder(v);
    }

    public void onBindViewHolder(@NonNull ChatListAdapter.MyViewHolder holder, int position) {
        Contact contact = list.get(position);
        
        // Handle contact name
        String contactName = contact.getFull_name();
        holder.contact_name.setText(contactName != null ? contactName : "Unknown Contact");

        String lastMessage = contact.getLast_message();
        Log.d("ChatListAdapter", "Contact: " + contact.getFull_name() + ", Last message: " + lastMessage);
        
        if (lastMessage == null || lastMessage.equals("")) {
            holder.contact_phone.setText("No messages yet");
        } else {
            // Check if it's a special message type that doesn't need decryption
            if (lastMessage.equals("sent an image")) {
                decryptedmessage = "📷 Image";
            } else if (lastMessage.equals("sent a voice message")) {
                decryptedmessage = "🎤 Voice message";
            } else if (lastMessage.equals("sent a file")) {
                decryptedmessage = "📎 File";
            } else {
                // Try to decrypt regular text messages using the fallback key method
                try {
                    decryptedmessage = CryptoHelper.decryptWithFallbackKey(lastMessage);
                    // If decryption results in empty string, show the original message
                    if (decryptedmessage == null || decryptedmessage.isEmpty()) {
                        decryptedmessage = lastMessage;
                    }
                } catch (Exception e) {
                    Log.d("ChatListAdapter", "Decryption failed: " + e.getMessage());
                    // If decryption fails, show a fallback message or the original
                    decryptedmessage = lastMessage.length() > 50 ? "Message" : lastMessage;
                }
            }
            
            Log.d("ChatListAdapter", "Decrypted message: " + decryptedmessage);
            
            String lastSenderName = contact.getLast_sender_name();
            if (lastSenderName != null && lastSenderName.equals("You")) {
                holder.contact_phone.setText(lastSenderName + " : " + decryptedmessage);
            } else {
                holder.contact_phone.setText(decryptedmessage);
            }
        }

        String lastMessageSeen = contact.getLast_message_seen();
        if (lastMessageSeen != null && lastMessageSeen.equals("false")) {
            holder.contact_phone.setTypeface(null, Typeface.BOLD);
        } else {
            holder.contact_phone.setTypeface(null, Typeface.NORMAL);
        }

        String profilePicUrl = contact.getProfilePic();
        String status = contact.getStatus();

        // Handle profile picture loading with error handling
        if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
            try {
                Picasso.get()
                    .load(profilePicUrl)
                    .placeholder(R.drawable.profile_pic)
                    .error(R.drawable.profile_pic)
                    .into(holder.profile_pic);
            } catch (Exception e) {
                Log.d("ChatListAdapter", "Error loading profile picture: " + e.getMessage());
                holder.profile_pic.setImageResource(R.drawable.profile_pic);
            }
        } else {
            // Set a default profile picture if URL is empty or null
            holder.profile_pic.setImageResource(R.drawable.profile_pic);
        }

        if (status != null && status.equals("active")) {
            holder.active_status.setVisibility(View.VISIBLE); // Set the online status indicator to visible
        } else {
            holder.active_status.setVisibility(View.INVISIBLE); // Set the online status indicator to invisible
        }
    }

    public int getItemCount() {
        if (list != null) {
            return list.size();
        } else {

            return -1;
        }
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView profile_pic, active_status;
        TextView contact_name;
        TextView contact_phone;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            profile_pic = itemView.findViewById(R.id.contact_image);
            contact_name = itemView.findViewById(R.id.contact_name);
            contact_phone = itemView.findViewById(R.id.contact_number);

            active_status = itemView.findViewById(R.id.online_status);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Context c = view.getContext();
                    Intent intent = new Intent(c, InboxActivity.class);
                    int position = getAdapterPosition();
                    Contact contact = list.get(position);
                    
                    // Add null check for contact uid
                    String contactUid = contact.getUid();
                    if (contactUid == null || contactUid.isEmpty()) {
                        Log.e("ChatListAdapter", "Contact UID is null or empty, cannot open chat");
                        return; // Don't start activity if UID is null
                    }
                    
                    intent.putExtra("uid", contactUid);
                    intent.putExtra("name", contact.getFull_name());
                    intent.putExtra("profilePic", contact.getProfilePic());
                    intent.putExtra("status", contact.getStatus());
                    contact_phone.setTypeface(null, Typeface.NORMAL);
                    c.startActivity(intent);
                }
            });
        }
    }
}
