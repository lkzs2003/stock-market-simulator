package org.example.market.model;

public abstract class User {
    private final String userId;
    private final String username;
    private final String password;
    private final String email;

    public User(String userId, String username, String password, String email) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public boolean login(String username, String password) {
        if (this.username.equals(username) && this.password.equals(password)) {
            System.out.println("User logged in.");
            return true;
        } else {
            System.out.println("Invalid login credentials.");
            return false;
        }
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }
}
