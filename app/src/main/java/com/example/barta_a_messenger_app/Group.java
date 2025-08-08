package com.example.barta_a_messenger_app;

import java.util.ArrayList;

public class Group {
    private String groupId;
    private String name;
    private String createdBy;
    private ArrayList<String> members;
    private long timestamp;

    public Group() {
        members = new ArrayList<>();
    }

    public Group(String name, String createdBy, ArrayList<String> members) {
        this.name = name;
        this.createdBy = createdBy;
        this.members = members;
        this.timestamp = System.currentTimeMillis();
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public ArrayList<String> getMembers() {
        return members;
    }

    public void setMembers(ArrayList<String> members) {
        this.members = members;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
} 