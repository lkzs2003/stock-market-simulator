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

    public void startSimulation() {
        running = true;
        startTime = System.currentTimeMillis() - elapsedTime; // Adjust start time with elapsed time
        for (FinancialInstrument instrument : market.getInstruments()) {
            executor.submit(new SimulationTask(instrument.getSymbol(), elapsedTime));
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
                // Odczyt upływu czasu symulacji
                elapsedTime = ((Double) state.get("elapsedTime")).longValue();

                // Odczyt cen instrumentów finansowych
                Map<String, Double> prices = (Map<String, Double>) state.get("prices");
                if (prices != null) {
                    for (FinancialInstrument instrument : market.getInstruments()) {
                        if (prices.containsKey(instrument.getSymbol())) {
                            BigDecimal price = BigDecimal.valueOf(prices.get(instrument.getSymbol()));
                            instrument.updatePrice(price);
                        }
                    }
                }

                // Odczyt portfela użytkownika
                Map<String, Double> portfolio = (Map<String, Double>) state.get("portfolio");
                if (portfolio != null) {
                    for (FinancialInstrument instrument : market.getInstruments()) {
                        if (portfolio.containsKey(instrument.getSymbol())) {
                            BigDecimal quantity = BigDecimal.valueOf(portfolio.get(instrument.getSymbol()));
                            trader.getPortfolio().addInstrument(instrument, quantity);
                        }
                    }
                }

                // Odczyt budżetu użytkownika
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

            // Zapisujemy upływ czasu symulacji
            state.put("elapsedTime", (System.currentTimeMillis() - startTime) / 1000.0);

            // Zapisujemy ceny instrumentów finansowych
            Map<String, BigDecimal> prices = new HashMap<>();
            for (FinancialInstrument instrument : market.getInstruments()) {
                prices.put(instrument.getSymbol(), instrument.getCurrentPrice());
            }
            state.put("prices", prices);

            // Zapisujemy portfel użytkownika
            Map<String, BigDecimal> portfolio = new HashMap<>();
            for (Map.Entry<FinancialInstrument, BigDecimal> entry : trader.getPortfolio().getInstruments().entrySet()) {
                portfolio.put(entry.getKey().getSymbol(), entry.getValue());
            }
            state.put("portfolio", portfolio);

            // Zapisujemy budżet użytkownika
            state.put("budget", trader.getBudget());

            gson.toJson(state, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateCurrentInstrumentData(String newInstrument) {
        executor.submit(new SimulationTask(newInstrument, elapsedTime));
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    private class SimulationTask implements Runnable {
        private final String instrumentSymbol;
        private final long initialElapsedTime;

        public SimulationTask(String instrumentSymbol, long initialElapsedTime) {
            this.instrumentSymbol = instrumentSymbol;
            this.initialElapsedTime = initialElapsedTime;
        }

        @Override
        public void run() {
            if (!running) return;

            for (StockDataPoint dataPoint : dataPoints) {
                if (dataPoint.getSymbol().equals(instrumentSymbol)) {
                    FinancialInstrument instrument = market.getInstrument(dataPoint.getSymbol());
                    if (instrument != null) {
                        instrument.updatePrice(dataPoint.getPrice());
                        long currentTime = (System.currentTimeMillis() - startTime) / 1000 + initialElapsedTime;
                        SwingUtilities.invokeLater(() -> {
                            marketGUI.updateCurrentPrice(instrument.getSymbol(), dataPoint.getPrice());
                            marketGUI.updateChart(instrument.getSymbol(), dataPoint.getPrice(), currentTime);
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
    }
}
