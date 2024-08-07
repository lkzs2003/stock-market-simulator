package org.example.market.simulation;

import org.example.market.data.StockDataLoader;
import org.example.market.data.StockDataPoint;
import org.example.market.GUI.LoginFrame;
import org.example.market.GUI.MainFrame;
import org.example.market.model.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // Wyświetl okno logowania i uzyskaj zalogowanego tradera
        LoginFrame loginFrame = new LoginFrame(null);
        loginFrame.setVisible(true);
        Trader trader = loginFrame.getAuthenticatedTrader();

        if (trader != null) {
            // Inicjalizacja obiektu Market
            Market market = initializeMarket();

            // Load market data
            List<String> filePaths = Arrays.asList(
                    "AAPL.csv",
                    "DELL.csv",
                    "EUR=X.csv",
                    "GBP=X.csv",
                    "MCD.csv",
                    "MSFT.csv",
                    "NFLX.csv",
                    "NKE.csv",
                    "SBUX.csv",
                    "TSLA.csv"
            );

            StockDataLoader dataLoader = new StockDataLoader();
            List<StockDataPoint> dataPoints = dataLoader.loadMultipleStockData(filePaths);

            // Wyświetl główne okno programu
            MainFrame mainFrame = new MainFrame(trader, market);
            MarketSimulator simulator = new MarketSimulator(dataPoints, market, mainFrame);
            mainFrame.setSimulator(simulator);
            simulator.startSimulation();
            mainFrame.setVisible(true);
        } else {
            System.out.println("Login failed or cancelled");
        }
    }

    private static Market initializeMarket() {
        Market market = new Market();
        market.addInstrumentsFromSymbols(Arrays.asList("AAPL", "DELL", "MCD", "MSFT", "NFLX", "NKE", "SBUX", "TSLA"));
        market.addInstrument(new Currency("EUR=X", "Euro", BigDecimal.ZERO, "Eurozone"));
        market.addInstrument(new Currency("GBP=X", "Pound", BigDecimal.ZERO, "United Kingdom"));
        return market;
    }
}
