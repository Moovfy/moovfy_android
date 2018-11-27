package com.moovfy.moovfy;

import android.net.Uri;

public class User {
    String email;
    String username;
    String name;
    Uri avatar;


    public User() {
    }

    public User(String email, String username, Uri avatar,String name) {
        this.email = email;
        this.username = username;
        this.avatar = avatar;
    }

    public String getEmail() {
        return email;
    }



    public String getUsername() {
        return username;
    }



    public Uri getAvatar() {
        return avatar;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setAvatar(Uri avatar) {
        this.avatar = avatar;
    }
}

