package com.example.barta_a_messenger_app;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {
    private List<Group> groupList;
    private Context context;

    public GroupAdapter(List<Group> groupList, Context context) {
        this.groupList = groupList;
        this.context = context;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        Group group = groupList.get(position);
        holder.groupName.setText(group.getName());
        holder.memberCount.setText(group.getMembers().size() + " members");

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, GroupInboxActivity.class);
            intent.putExtra("groupId", group.getGroupId());
            intent.putExtra("groupName", group.getName());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }

    public void updateGroups(List<Group> newGroups) {
        groupList.clear();
        groupList.addAll(newGroups);
        notifyDataSetChanged();
    }

    static class GroupViewHolder extends RecyclerView.ViewHolder {
        CircleImageView groupProfile;
        TextView groupName;
        TextView memberCount;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            groupProfile = itemView.findViewById(R.id.group_profile);
            groupName = itemView.findViewById(R.id.group_name);
            memberCount = itemView.findViewById(R.id.member_count);
        }
    }
} 