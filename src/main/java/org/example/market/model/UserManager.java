package org.example.market.model;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserManager {
    private Map<String, User> users;

    public UserManager() {
        this.users = new HashMap<>();
    }

    public void addUser(User user) {
        users.put(user.getUserId(), user);
    }

    public User getUser(String userId) {
        return users.get(userId);
    }

    public User getUserByUsername(String username) {
        for (User user : users.values()) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }

    public boolean authenticate(String username, String password) {
        for (User user : users.values()) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                return true;
            }
        }
        return false;
    }

    public boolean validatePassword(String username, String password) {
        for (User user : users.values()) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                return true;
            }
        }
        return false;
    }

    public boolean userExists(String username) {
        for (User user : users.values()) {
            if (user.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    public void registerUser(String username, String password, String email) {
        String userId = UUID.randomUUID().toString();
        if (!userExists(username)) {
            User user = new GeneralUser(userId, username, password, email);
            addUser(user);
            System.out.println("User " + username + " registered successfully.");
        } else {
            System.out.println("User " + username + " already exists.");
        }
    }

    public void registerTrader(String userId, String username, String password, String email, int riskAssessmentLevel) {
        if (!userExists(username)) {
            Trader trader = new StockTrader(userId, username, password, email, riskAssessmentLevel);
            addUser(trader);
            System.out.println("Trader " + username + " registered successfully.");
        } else {
            System.out.println("Trader " + username + " already exists.");
        }
    }
}
