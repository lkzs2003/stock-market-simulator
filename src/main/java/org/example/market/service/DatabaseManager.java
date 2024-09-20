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
        // SQL to create the simulation_state table if it does not exist
        String createSimulationStateTable = "CREATE TABLE IF NOT EXISTS simulation_state ("
                + "user_id TEXT PRIMARY KEY, "
                + "elapsed_time INTEGER, "
                + "instrument_symbol TEXT, "
                + "price REAL, "
                + "quantity REAL, "
                + "budget REAL"
                + ");";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createSimulationStateTable); // Ensure the simulation_state table exists
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
