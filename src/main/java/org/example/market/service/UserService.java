package org.example.market.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.example.market.model.Trader;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class UserService {
    private static final String USER_FILE = "users.json";
    private Map<String, Trader> users = new HashMap<>();
    private final Gson gson;

    public UserService() {
        gson = new GsonBuilder()
                .registerTypeAdapter(Trader.class, new TraderAdapter())
                .create();
        loadUsersFromFile();
    }

    public boolean registerUser(Trader trader) {
        if (users.containsKey(trader.getUsername())) {
            System.out.println("User already exists.");
            return false;
        }
        users.put(trader.getUsername(), trader);
        saveUsersToFile();
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

    private void loadUsersFromFile() {
        try (Reader reader = new FileReader(USER_FILE)) {
            Type userMapType = new TypeToken<Map<String, Trader>>(){}.getType();
            users = gson.fromJson(reader, userMapType);
            if (users == null) {
                users = new HashMap<>();
            }
        } catch (FileNotFoundException e) {
            System.out.println("No user file found. Starting with an empty user list.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveUsersToFile() {
        try (Writer writer = new FileWriter(USER_FILE)) {
            gson.toJson(users, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
