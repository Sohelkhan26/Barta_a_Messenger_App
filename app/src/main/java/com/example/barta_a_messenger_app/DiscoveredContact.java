package com.example.barta_a_messenger_app;

public class DiscoveredContact {
    private String name;
    private String phoneNumber;
    private boolean isAdded;

    public DiscoveredContact(String name, String phoneNumber) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.isAdded = false;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public boolean isAdded() {
        return isAdded;
    }

    public void setAdded(boolean added) {
        isAdded = added;
    }
} 