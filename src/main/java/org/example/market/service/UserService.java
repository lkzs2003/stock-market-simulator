package org.example.market.service;

import org.example.market.model.StockTrader;
import org.example.market.model.Trader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class UserService {

    public UserService() {
        DatabaseManager.initializeDatabase();  // Ensure the database and tables are created
    }

    // Register a new user (Trader)
    public boolean registerUser(Trader trader) {
        // Check if the user already exists
        if (userExists(trader.getUsername())) {
            System.out.println("User already exists.");
            return false;
        }

        String insertSQL = "INSERT INTO users (user_id, username, password, email) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {

            pstmt.setString(1, UUID.randomUUID().toString()); // Generate a unique user_id
            pstmt.setString(2, trader.getUsername());
            pstmt.setString(3, trader.getPassword());
            pstmt.setString(4, trader.getEmail());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Check if a user with the provided username already exists
    public boolean userExists(String username) {
        String checkUserSQL = "SELECT 1 FROM users WHERE username = ?";
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(checkUserSQL)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            return rs.next(); // If a row exists, the user already exists
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Log in a user by checking credentials
    public Trader loginUser(String username, String password) {
        String selectSQL = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(selectSQL)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // Assuming the user is always a StockTrader for simplicity
                return new StockTrader(
                        rs.getString("user_id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("email")
                );
            } else {
                return null; // No matching user found
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
