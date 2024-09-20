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

            if (rs.next()) {
                // Load the elapsed time
                elapsedTime = rs.getLong("elapsed_time");

                // Load the prices of the financial instruments
                String symbol = rs.getString("instrument_symbol");
                BigDecimal price = rs.getBigDecimal("price");
                FinancialInstrument instrument = market.getInstrument(symbol);
                if (instrument != null) {
                    instrument.updatePrice(price);
                }

                // Load the trader's portfolio
                BigDecimal quantity = rs.getBigDecimal("quantity");
                if (instrument != null && quantity != null) {
                    trader.getPortfolio().addInstrument(instrument, quantity);  // Ensure correct portfolio addition
                }

                // Load the trader's budget
                BigDecimal budget = rs.getBigDecimal("budget");
                if (budget != null) {
                    trader.setBudget(budget);  // Ensure budget is loaded
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void saveSimulationState() {
        String insertSQL = "INSERT OR REPLACE INTO simulation_state (user_id, elapsed_time, instrument_symbol, price, quantity, budget) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {

            pstmt.setString(1, trader.getUserId());
            pstmt.setLong(2, (System.currentTimeMillis() - startTime));

            for (FinancialInstrument instrument : market.getInstruments()) {
                pstmt.setString(3, instrument.getSymbol());
                pstmt.setBigDecimal(4, instrument.getCurrentPrice());
                pstmt.setBigDecimal(5, trader.getPortfolio().getQuantity(instrument));
                pstmt.setBigDecimal(6, trader.getBudget());
                pstmt.executeUpdate();
            }
            System.out.println("Simulation state saved successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
