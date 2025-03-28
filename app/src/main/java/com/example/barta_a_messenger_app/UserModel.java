package com.example.barta_a_messenger_app;

public class UserModel {
    private String uid;
    private String username;
    private String profilePic;
    private String email;

    public UserModel() {
        // Required empty constructor for Firebase
    }

    public UserModel(String uid, String username, String profilePic, String email) {
        this.uid = uid;
        this.username = username;
        this.profilePic = profilePic;
        this.email = email;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
} 