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

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Contact> list;
    private OnItemClickListener listener;

    // Interface for click listener
    public interface OnItemClickListener {

        void onItemClick(Contact contact);
    }

    public ContactAdapter(Context context, ArrayList<Contact> list) {
        this.context = context;
        this.list = list;
    }

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
}
