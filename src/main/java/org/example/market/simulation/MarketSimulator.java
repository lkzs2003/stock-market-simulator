package org.example.market.simulation;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.example.market.GUI.MarketGUI;
import org.example.market.data.StockDataPoint;
import org.example.market.model.FinancialInstrument;
import org.example.market.model.Market;
import org.example.market.model.StockTrader;

import javax.swing.SwingUtilities;
import java.io.*;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private static final String SIMULATION_STATE_FILE = "simulation_state.json";

    public MarketSimulator(List<StockDataPoint> dataPoints, Market market, StockTrader trader, MarketGUI marketGUI) {
        this.dataPoints = dataPoints;
        this.market = market;
        this.trader = trader;
        this.marketGUI = marketGUI;
        this.executor = Executors.newFixedThreadPool(10);
        this.running = true;

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
        // Get the current time and subtract the elapsed time to continue the simulation from the previous moment
        startTime = System.currentTimeMillis() - elapsedTime; // Subtract the elapsed time to continue the simulation

        // Start the simulation for each instrument in the market
        for (FinancialInstrument instrument : market.getInstruments()) {
            executor.submit(() -> runSimulationForInstrument(instrument.getSymbol(), elapsedTime));
        }
    }

    private void runSimulationForInstrument(String instrumentSymbol, long initialElapsedTime) {
        if (!running) return;

        for (StockDataPoint dataPoint : dataPoints) {
            if (!running) break; // Ensure the task stops if simulation is stopped

            if (dataPoint.getSymbol().equals(instrumentSymbol)) {
                FinancialInstrument instrument = market.getInstrument(dataPoint.getSymbol());
                if (instrument != null) {
                    instrument.updatePrice(dataPoint.getPrice());
                    long currentTime = (System.currentTimeMillis() - startTime) + initialElapsedTime;

                    // Update the GUI with the new data point
                    SwingUtilities.invokeLater(() -> {
                        marketGUI.updateCurrentPrice(instrument.getSymbol(), dataPoint.getPrice());

                        // Dodanie punktu danych do serii wykresu
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
        saveSimulationState(); // Save the state when stopping the simulation
    }

    private void loadSimulationState() {
        File file = new File(SIMULATION_STATE_FILE);
        System.out.println("Attempting to load simulation state from: " + file.getAbsolutePath());
        if (!file.exists()) {
            System.out.println("Simulation state file does not exist.");
            return;
        }

        try (Reader reader = new FileReader(SIMULATION_STATE_FILE)) {
            Gson gson = new Gson();
            Type stateType = new TypeToken<Map<String, Object>>() {}.getType();
            Map<String, Object> state = gson.fromJson(reader, stateType);

            if (state != null) {
                // Get the elapsed time of the simulation
                elapsedTime = ((Double) state.get("elapsedTime")).longValue();

                // Get the prices of the financial instruments
                Map<String, Double> prices = (Map<String, Double>) state.get("prices");
                if (prices != null) {
                    for (FinancialInstrument instrument : market.getInstruments()) {
                        if (prices.containsKey(instrument.getSymbol())) {
                            BigDecimal price = BigDecimal.valueOf(prices.get(instrument.getSymbol()));
                            instrument.updatePrice(price);
                        }
                    }
                }

                // Get the user's portfolio
                Map<String, Double> portfolio = (Map<String, Double>) state.get("portfolio");
                if (portfolio != null) {
                    for (FinancialInstrument instrument : market.getInstruments()) {
                        if (portfolio.containsKey(instrument.getSymbol())) {
                            BigDecimal quantity = BigDecimal.valueOf(portfolio.get(instrument.getSymbol()));
                            trader.getPortfolio().addInstrument(instrument, quantity);
                        }
                    }
                }

                // Get the user's budget
                Double budget = (Double) state.get("budget");
                if (budget != null) {
                    trader.setBudget(BigDecimal.valueOf(budget));
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("No previous simulation state found. Starting fresh.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveSimulationState() {
        try (Writer writer = new FileWriter(SIMULATION_STATE_FILE)) {
            System.out.println("Saving simulation state to: " + new File(SIMULATION_STATE_FILE).getAbsolutePath());
            Gson gson = new Gson();
            Map<String, Object> state = new HashMap<>();

            // Save the elapsed time of the simulation
            state.put("elapsedTime", (System.currentTimeMillis() - startTime));

            // Save the current prices of the financial instruments
            Map<String, BigDecimal> prices = new HashMap<>();
            for (FinancialInstrument instrument : market.getInstruments()) {
                prices.put(instrument.getSymbol(), instrument.getCurrentPrice());
            }
            state.put("prices", prices);

            // Save the user's portfolio
            Map<String, BigDecimal> portfolio = new HashMap<>();
            for (Map.Entry<FinancialInstrument, BigDecimal> entry : trader.getPortfolio().getInstruments().entrySet()) {
                portfolio.put(entry.getKey().getSymbol(), entry.getValue());
            }
            state.put("portfolio", portfolio);

            // Save the user's budget
            state.put("budget", trader.getBudget());

            gson.toJson(state, writer);
            System.out.println("Simulation state saved successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
