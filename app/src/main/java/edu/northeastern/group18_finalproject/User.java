package edu.northeastern.group18_finalproject;

import java.util.List;

public class User {
    private String username;
    private List<String> friends;

    public User() {
    }

    public User(String username, List<String> friends) {
        this.username = username;
        this.friends = friends;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getFriends() {
        return friends;
    }

    public void setFriends(List<String> friends) {
        this.friends = friends;
    }
}

