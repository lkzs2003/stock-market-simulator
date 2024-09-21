package org.example.market.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String DATABASE_URL = "jdbc:sqlite:market_simulator.db";

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(DATABASE_URL);
    }

    public static void initializeDatabase() {
        // SQL to create the users table if it does not exist
        String createUsersTable = "CREATE TABLE IF NOT EXISTS users ("
                + "user_id TEXT PRIMARY KEY, "
                + "username TEXT NOT NULL UNIQUE, "
                + "password TEXT NOT NULL, "
                + "email TEXT NOT NULL"
                + ");";

        // SQL to create the simulation_state table with a composite primary key
        String createSimulationStateTable = "CREATE TABLE IF NOT EXISTS simulation_state ("
                + "user_id TEXT NOT NULL, "
                + "instrument_symbol TEXT NOT NULL, "
                + "elapsed_time INTEGER, "
                + "price REAL, "
                + "quantity REAL, "
                + "budget REAL, "
                + "PRIMARY KEY (user_id, instrument_symbol)"  // Composite primary key
                + ");";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            // Execute the SQL to create the users table
            stmt.execute(createUsersTable);
            // Execute the SQL to create the simulation_state table with composite key
            stmt.execute(createSimulationStateTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
