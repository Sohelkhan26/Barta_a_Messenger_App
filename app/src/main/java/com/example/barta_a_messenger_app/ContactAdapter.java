package com.example.barta_a_messenger_app;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

<<<<<<< HEAD
public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Contact> list;
    private OnItemClickListener listener;

    // Interface for click listener
    public interface OnItemClickListener {

        void onItemClick(Contact contact);
    }
=======
public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.MyViewHolder>{
//    private ClickListener clickListener;

    Context context;
    static ArrayList<Contact> list;

>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c

    public ContactAdapter(Context context, ArrayList<Contact> list) {
        this.context = context;
        this.list = list;
    }

<<<<<<< HEAD
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.contact_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Contact contact = list.get(position);
        holder.name.setText(contact.getFull_name());
        holder.phone.setText(contact.getPhone_number());
        holder.status.setText(contact.getStatus());

        // Load profile picture using Picasso
        if (contact.getProfilePic() != null && !contact.getProfilePic().isEmpty()) {
            Picasso.get().load(contact.getProfilePic())
                    .placeholder(R.drawable.default_profile)
                    .into(holder.profile);
        } else {
            holder.profile.setImageResource(R.drawable.default_profile);
        }

        // Set click listener
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, InboxActivity.class);
                intent.putExtra("uid", contact.getUid());
                intent.putExtra("full_name", contact.getFull_name());
                intent.putExtra("profilePic", contact.getProfilePic());
                intent.putExtra("phone", contact.getPhone_number());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView name, phone, status;
        ImageView profile;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.contact_name);
            phone = itemView.findViewById(R.id.contact_phone);
            status = itemView.findViewById(R.id.contact_status);
            profile = itemView.findViewById(R.id.contact_profile);
        }
    }
=======
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.contacts,parent,false);
        return new MyViewHolder(v);
    }


    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Contact contact = list.get(position);
        holder.contact_name.setText(contact.getFull_name());
        holder.contact_phone.setText(contact.getPhone_number());


        String profilePicUrl = contact.getProfilePic();
        String status = contact.getStatus();

        if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
            Picasso.get().load(profilePicUrl).into(holder.profile_pic);
        }
        else
        {
            // Handle the case where the URL is empty or null
        }

        if (status.equals("active")) {
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
        if(list!=null){
            return list.size();
        }
        else{
            return -1;
        }
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        ImageView profile_pic,active_status;
        TextView contact_name;
        TextView contact_phone;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            profile_pic=itemView.findViewById(R.id.contact_image);
            contact_name=itemView.findViewById(R.id.contact_name);
            contact_phone=itemView.findViewById(R.id.contact_number);
            active_status = itemView.findViewById(R.id.online_status);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Context c = view.getContext();
                    Intent intent = new Intent(c,InboxActivity.class);
                    int position = getAdapterPosition();
                    Contact contact = list.get(position);
                    intent.putExtra("Name",contact.getFull_name());
                    intent.putExtra("phone_no",contact.getPhone_number());
                    intent.putExtra("contact_uid",contact.getUid());
                    intent.putExtra("profile_pic",contact.getProfilePic());

                    c.startActivity(intent);
                }
            });
        }
    }
//    public interface ClickListener{
//        void onItemClick(int position);
//    }

//    public void setOnItemClickListener(ClickListener clickListener){
//        this.clickListener = clickListener;
//    }

>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c
}
