package com.example.portfolio;

public class User {
    private final String username;
    private final int id;

    public User(String username, int id) {
        this.username = username;
        this.id = id;
    }

    public String getUsername() {
        return username;
    }
    public int getId() { return id;}
}

