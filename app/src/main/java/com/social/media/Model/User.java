package com.social.media.Model;

public class User {
    private String name , profession , email, password;
    private String coverPhoto;
    private String userID;
    private String profile;
    private String status;
    private String bio;

    public User() {
    }

    public String getCoverPhoto() {
        return coverPhoto;
    }

    public void setCoverPhoto(String coverPhoto) {
        this.coverPhoto = coverPhoto;
    }

    public User(String name, String profession, String email, String password, String coverPhoto, String userID, String bio, String profile, String status) {
        this.name = name;
        this.profession = profession;
        this.email = email;
        this.password = password;
        this.coverPhoto = coverPhoto;
        this.userID = userID;
        this.bio = bio;
        this.profile = profile;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getBio() {
        return bio;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}
