package com.example.barta_a_messenger_app;

public class ForwardContact {
    private String uid;
    private String name;
    private String profilePic;
    private String status;
    private boolean isSelected;

    public ForwardContact() {
        // Default constructor required for Firebase
    }

    public ForwardContact(String uid, String name, String profilePic, String status, boolean isSelected) {
        this.uid = uid;
        this.name = name;
        this.profilePic = profilePic;
        this.status = status;
        this.isSelected = isSelected;
    }

    // Getters and setters
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ForwardContact that = (ForwardContact) obj;
        return uid != null ? uid.equals(that.uid) : that.uid == null;
    }

    @Override
    public int hashCode() {
        return uid != null ? uid.hashCode() : 0;
    }
}
