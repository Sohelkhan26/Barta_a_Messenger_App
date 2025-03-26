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
<<<<<<< HEAD

=======
>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c
    Context context;
    static ArrayList<Contact> list;
    String decryptedmessage;

<<<<<<< HEAD
=======

>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c
    public ChatListAdapter(Context context, ArrayList<Contact> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
<<<<<<< HEAD
        View v = LayoutInflater.from(context).inflate(R.layout.contacts, parent, false);
        return new ChatListAdapter.MyViewHolder(v);
    }

=======
        View v = LayoutInflater.from(context).inflate(R.layout.contacts,parent,false);
        return new ChatListAdapter.MyViewHolder(v);
    }


>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c
    public void onBindViewHolder(@NonNull ChatListAdapter.MyViewHolder holder, int position) {
        Contact contact = list.get(position);
        holder.contact_name.setText(contact.getFull_name());

<<<<<<< HEAD
        try {
            decryptedmessage = CryptoHelper.decrypt("H@rrY_p0tter_106", contact.getLast_message());
        } catch (Exception e) {
            Log.d("ChatListAdapter ", e.getMessage());
        }

        if (contact.getLast_message().equals("")) {
            holder.contact_phone.setText("");
        } else {
            if (contact.getLast_sender_name().equals("You")) {
                holder.contact_phone.setText(contact.getLast_sender_name() + " : " + decryptedmessage);
            } else {
=======
        try{
            decryptedmessage = CryptoHelper.decrypt("H@rrY_p0tter_106",contact.getLast_message());
        }
        catch (Exception e) {
            Log.d("ChatListAdapter ", e.getMessage());
        }

        if(contact.getLast_message().equals("")){
            holder.contact_phone.setText("");
        }
        else{
            if(contact.getLast_sender_name().equals("You")){
                holder.contact_phone.setText(contact.getLast_sender_name()+" : "+decryptedmessage);
            }
            else{
>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c
                holder.contact_phone.setText(decryptedmessage);
            }
        }

<<<<<<< HEAD
        if (contact.getLast_message_seen().equals("false")) {
            holder.contact_phone.setTypeface(null, Typeface.BOLD);
        } else {
            holder.contact_phone.setTypeface(null, Typeface.NORMAL);
        }
=======
        if(contact.getLast_message_seen().equals("false")){
            holder.contact_phone.setTypeface(null, Typeface.BOLD);
        }
        else{
            holder.contact_phone.setTypeface(null,Typeface.NORMAL);
        }

>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c

        String profilePicUrl = contact.getProfilePic();
        String status = contact.getStatus();

        if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
            Picasso.get().load(profilePicUrl).into(holder.profile_pic);
<<<<<<< HEAD
        } else {
=======
        }
        else
        {
>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c
            // Handle the case where the URL is empty or null
        }

        if (status.equals("active")) {
            holder.active_status.setVisibility(View.VISIBLE); // Set the online status indicator to visible
        } else {
            holder.active_status.setVisibility(View.INVISIBLE); // Set the online status indicator to invisible
        }

<<<<<<< HEAD
//        Picasso.get().load(contact.getProfilePic()).into(holder.profile_pic);
=======

//        Picasso.get().load(contact.getProfilePic()).into(holder.profile_pic);

>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c
//        ImageView alertImageView = holder.itemView.findViewById(R.id.danger);
//
//        if (record.shouldShowAlert()) {
//            alertImageView.setVisibility(View.VISIBLE);
//        } else {
//            alertImageView.setVisibility(View.INVISIBLE);
//        }
    }

    public int getItemCount() {
<<<<<<< HEAD
        if (list != null) {
            return list.size();
        } else {
=======
        if(list!=null){
            return list.size();
        }
        else{
>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c
            return -1;
        }
    }

<<<<<<< HEAD
    public static class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView profile_pic, active_status;
        TextView contact_name;
        TextView contact_phone;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            profile_pic = itemView.findViewById(R.id.contact_image);
            contact_name = itemView.findViewById(R.id.contact_name);
            contact_phone = itemView.findViewById(R.id.contact_number);
=======
    public static class MyViewHolder extends RecyclerView.ViewHolder{
        ImageView profile_pic,active_status;
        TextView contact_name;
        TextView contact_phone;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            profile_pic=itemView.findViewById(R.id.contact_image);
            contact_name=itemView.findViewById(R.id.contact_name);
            contact_phone=itemView.findViewById(R.id.contact_number);
>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c
            active_status = itemView.findViewById(R.id.online_status);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Context c = view.getContext();
<<<<<<< HEAD
                    Intent intent = new Intent(c, InboxActivity.class);
                    int position = getAdapterPosition();
                    Contact contact = list.get(position);
                    intent.putExtra("uid", contact.getUid());
                    intent.putExtra("name", contact.getFull_name());
                    intent.putExtra("profilePic", contact.getProfilePic());
                    intent.putExtra("status", contact.getStatus());
                    contact_phone.setTypeface(null, Typeface.NORMAL);
=======
                    Intent intent = new Intent(c,InboxActivity.class);
                    int position = getAdapterPosition();
                    Contact contact = list.get(position);
                    intent.putExtra("Name",contact.getFull_name());
                    intent.putExtra("phone_no",contact.getPhone_number());
                    intent.putExtra("contact_uid",contact.getUid());
                    contact_phone.setTypeface(null,Typeface.NORMAL);
>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c
                    c.startActivity(intent);
                }
            });
        }
    }
}
