package org.example.market.service;

import org.example.market.model.Trader;

import java.util.HashMap;
import java.util.Map;

public class UserService {
    private final Map<String, Trader> users = new HashMap<>();

    public boolean registerUser(Trader trader) {
        if (users.containsKey(trader.getUsername())) {
            System.out.println("User already exists.");
            return false;
        }
        users.put(trader.getUsername(), trader);
        System.out.println("User registered successfully.");
        return true;
    }

    public Trader loginUser(String username, String password) {
        Trader trader = users.get(username);
        if (trader != null && trader.login(username, password)) {
            System.out.println("User logged in successfully.");
            return trader;
        }
        System.out.println("Invalid username or password.");
        return null;
    }

    public boolean userExists(String username) {
        return users.containsKey(username);
    }
}
