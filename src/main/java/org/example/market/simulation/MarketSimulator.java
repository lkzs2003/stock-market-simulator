package org.example.market.simulation;

import org.example.market.data.StockDataPoint;
import org.example.market.GUI.MarketGUI;
import org.example.market.model.FinancialInstrument;
import org.example.market.model.Market;

import javax.swing.SwingUtilities;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MarketSimulator {
    private final List<StockDataPoint> dataPoints;
    private final ExecutorService executor;
    private MarketGUI marketGUI;
    private final Market market;
    private volatile boolean running;
    private long startTime;

    public MarketSimulator(List<StockDataPoint> dataPoints, Market market, MarketGUI marketGUI) {
        this.dataPoints = dataPoints;
        this.market = market;
        this.marketGUI = marketGUI;
        this.executor = Executors.newFixedThreadPool(10);
        this.running = true;
    }

    public void setMarketGUI(MarketGUI marketGUI) {
        this.marketGUI = marketGUI;
    }

    public void startSimulation() {
        running = true;
        startTime = System.currentTimeMillis();
        for (FinancialInstrument instrument : market.getInstruments()) {
            executor.submit(new SimulationTask(instrument.getSymbol()));
        }
    }

    public void updateCurrentInstrumentData(String newInstrument) {
        executor.submit(new SimulationTask(newInstrument));
    }

    private class SimulationTask implements Runnable {
        private final String instrumentSymbol;

        public SimulationTask(String instrumentSymbol) {
            this.instrumentSymbol = instrumentSymbol;
        }

        @Override
        public void run() {
            if (!running) return;

            for (StockDataPoint dataPoint : dataPoints) {
                if (dataPoint.getSymbol().equals(instrumentSymbol)) {
                    FinancialInstrument instrument = market.getInstrument(dataPoint.getSymbol());
                    if (instrument != null) {
                        instrument.updatePrice(dataPoint.getPrice());
                        SwingUtilities.invokeLater(() -> {
                            long elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
                            marketGUI.updateCurrentPrice(instrument.getSymbol(), dataPoint.getPrice());
                            if (instrument.getSymbol().equals(marketGUI.getCurrentInstrument())) {
                                marketGUI.updateChart(instrument.getSymbol(), dataPoint.getPrice(), elapsedTime);
                            }
                        });
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            }
        }
    }
}