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
        holder.contact_name.setText(contact.getFull_name());

        try {
            String lastMessage = contact.getLast_message();
            if (lastMessage != null && !lastMessage.isEmpty()) {
                decryptedmessage = CryptoHelper.decrypt("H@rrY_p0tter_106", lastMessage);
            } else {
                decryptedmessage = "";
            }
        } catch (Exception e) {
            Log.d("ChatListAdapter ", e.getMessage());
            decryptedmessage = "";
        }

        String lastMessage = contact.getLast_message();
        if (lastMessage == null || lastMessage.equals("")) {
            holder.contact_phone.setText("");
        } else {
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

        if (profilePicUrl != null && !profilePicUrl.isEmpty()) {

        } else {

            // Handle the case where the URL is empty or null
        }

        if (status != null && status.equals("active")) {
            holder.active_status.setVisibility(View.VISIBLE); // Set the online status indicator to visible
        } else {
            holder.active_status.setVisibility(View.INVISIBLE); // Set the online status indicator to invisible
        }

//        Picasso.get().load(contact.getProfilePic()).into(holder.profile_pic);
//        ImageView alertImageView = holder.itemView.findViewById(R.id.danger);
//
//        if (record.shouldShowAlert()) {
//            alertImageView.setVisibility(View.VISIBLE);
//        } else {
//            alertImageView.setVisibility(View.INVISIBLE);
//        }
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
