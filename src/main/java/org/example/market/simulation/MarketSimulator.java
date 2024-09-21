package org.example.market.simulation;

import org.example.market.GUI.MarketGUI;
import org.example.market.data.StockDataPoint;
import org.example.market.model.FinancialInstrument;
import org.example.market.model.Market;
import org.example.market.model.StockTrader;
import org.example.market.service.DatabaseManager;

import javax.swing.SwingUtilities;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.jfree.data.xy.XYSeries;

public class MarketSimulator {
    private final List<StockDataPoint> dataPoints;
    private final ExecutorService executor;
    private MarketGUI marketGUI;
    private final Market market;
    private final StockTrader trader;
    private volatile boolean running;
    private long startTime;
    private long elapsedTime;

    public MarketSimulator(List<StockDataPoint> dataPoints, Market market, StockTrader trader, MarketGUI marketGUI) {
        this.dataPoints = dataPoints;
        this.market = market;
        this.trader = trader;
        this.marketGUI = marketGUI;
        this.executor = Executors.newFixedThreadPool(10);
        this.running = true;

        DatabaseManager.initializeDatabase(); // Ensure database and table creation
        loadSimulationState(); // Load the previous state if it exists
    }

    public void setMarketGUI(MarketGUI marketGUI) {
        this.marketGUI = marketGUI;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public void startSimulation() {
        running = true;
        startTime = System.currentTimeMillis() - elapsedTime;

        // Ensure all instruments are initialized with correct prices before starting the simulation
        for (FinancialInstrument instrument : market.getInstruments()) {
            if (instrument.getCurrentPrice() == null) {
                instrument.updatePrice(BigDecimal.ZERO); // Initialize price if not set
            }
        }

        // Start the simulation for each instrument in the market
        for (FinancialInstrument instrument : market.getInstruments()) {
            executor.submit(() -> runSimulationForInstrument(instrument.getSymbol(), elapsedTime));
        }
    }

    private void runSimulationForInstrument(String instrumentSymbol, long initialElapsedTime) {
        if (!running) return;

        for (StockDataPoint dataPoint : dataPoints) {
            if (!running) break;

            if (dataPoint.getSymbol().equals(instrumentSymbol)) {
                FinancialInstrument instrument = market.getInstrument(dataPoint.getSymbol());
                if (instrument != null) {
                    instrument.updatePrice(dataPoint.getPrice());
                    long currentTime = (System.currentTimeMillis() - startTime) + initialElapsedTime;

                    // Update the GUI with the new data point
                    SwingUtilities.invokeLater(() -> {
                        marketGUI.updateCurrentPrice(instrument.getSymbol(), dataPoint.getPrice());

                        // Add the new data point to the chart series
                        XYSeries series = marketGUI.getSeriesMap().get(instrument.getSymbol());
                        if (series != null) {
                            series.add(currentTime / 1000.0, instrument.getCurrentPrice().doubleValue());
                        }

                        marketGUI.updateChart(instrument.getSymbol(), dataPoint.getPrice(), currentTime / 1000);
                    });

                    try {
                        Thread.sleep(1000); // Simulate time delay for new data points
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }

    public void stopSimulation() {
        running = false;
        saveSimulationState(); // Save the state to SQLite when stopping the simulation
    }

    private void loadSimulationState() {
        String selectSQL = "SELECT * FROM simulation_state WHERE user_id = ?";
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(selectSQL)) {

            pstmt.setString(1, trader.getUserId());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                // Załaduj czas symulacji
                elapsedTime = rs.getLong("elapsed_time");
                startTime = System.currentTimeMillis() - elapsedTime;  // Wznów symulację od ostatniego zapisanego czasu

                // Załaduj każdy instrument: symbol, cenę, ilość oraz budżet
                String symbol = rs.getString("instrument_symbol");
                BigDecimal price = rs.getBigDecimal("price");
                BigDecimal quantity = rs.getBigDecimal("quantity");
                BigDecimal budget = rs.getBigDecimal("budget");

                FinancialInstrument instrument = market.getInstrument(symbol);
                if (instrument != null) {
                    instrument.updatePrice(price);
                    System.out.println("Loaded instrument: " + symbol + ", Quantity: " + quantity + ", Price: " + price);
                }

                // Dodaj instrumenty z ważną ilością do portfela
                if (quantity != null && quantity.compareTo(BigDecimal.ZERO) > 0 && instrument != null) {
                    trader.getPortfolio().addInstrument(instrument, quantity);
                }

                // Ustaw budżet użytkownika
                if (budget != null) {
                    trader.setBudget(budget);
                }

                System.out.println("Loaded portfolio for User ID: " + trader.getUserId());
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void saveSimulationState() {
        String insertSQL = "INSERT OR REPLACE INTO simulation_state (user_id, elapsed_time, instrument_symbol, price, quantity, budget) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        try {
            conn = DatabaseManager.connect();
            conn.setAutoCommit(false);  // Wyłącz tryb automatycznego zatwierdzania dla transakcji

            PreparedStatement pstmt = conn.prepareStatement(insertSQL);
            long currentElapsedTime = System.currentTimeMillis() - startTime;

            // Ustaw podstawowe informacje o użytkowniku i czasie symulacji
            for (FinancialInstrument instrument : market.getInstruments()) {
                BigDecimal quantity = trader.getPortfolio().getQuantity(instrument);
                BigDecimal price = instrument.getCurrentPrice();
                BigDecimal budget = trader.getBudget();

                // Sprawdź, czy ilość jest niezerowa i niepusta
                if (quantity != null && quantity.compareTo(BigDecimal.ZERO) > 0) {
                    System.out.println("Saving portfolio: User ID = " + trader.getUserId());
                    System.out.println("Instrument = " + instrument.getSymbol() + ", Quantity = " + quantity + ", Price = " + price);
                    System.out.println("Budget = " + budget);

                    pstmt.setString(1, trader.getUserId());
                    pstmt.setLong(2, currentElapsedTime);
                    pstmt.setString(3, instrument.getSymbol());
                    pstmt.setBigDecimal(4, price);
                    pstmt.setBigDecimal(5, quantity);
                    pstmt.setBigDecimal(6, budget);
                    pstmt.executeUpdate();  // Zapisz dane do bazy
                }
            }

            conn.commit();  // Zatwierdź transakcję po zapisaniu wszystkich instrumentów
            System.out.println("Simulation state saved successfully.");
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();  // Cofnij transakcję w przypadku błędu
                    System.out.println("Transaction rolled back due to error.");
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);  // Przywróć tryb automatycznego zatwierdzania
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
