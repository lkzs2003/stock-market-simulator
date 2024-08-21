package org.example.market.GUI;

import org.example.market.model.FinancialInstrument;
import org.example.market.model.Market;
import org.example.market.model.StockTrader;
import org.example.market.simulation.MarketSimulator;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MainFrame extends JFrame implements MarketGUI {
    private final StockTrader trader;
    private final Market market;
    private MarketSimulator simulator;
    private JComboBox<String> instrumentComboBox;
    private JTextField quantityField;
    private JLabel currentPriceLabel;
    private JLabel budgetLabel;
    private DefaultTableModel portfolioTableModel;
    private final Map<String, XYSeries> seriesMap;
    private final Map<String, XYSeriesCollection> datasetMap;
    private ChartPanel chartPanel;
    private String currentInstrument;

    public MainFrame(StockTrader trader, Market market, MarketSimulator simulator) {
        this.trader = trader;
        this.market = market;
        this.simulator = simulator;
        this.seriesMap = new ConcurrentHashMap<>();
        this.datasetMap = new ConcurrentHashMap<>();
        this.currentInstrument = market.getInstruments().getFirst().getSymbol(); // Set first available instrument
        initialize();
    }

    private void initialize() {
        setTitle("Stock Market Simulator");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        addWindowListener(new java.awt.event.WindowAdapter() {
        @Override
        public void windowClosing(java.awt.event.WindowEvent windowEvent) {
            if (simulator != null) {
                simulator.stopSimulation(); // Stop the simulation when the window is closed
            }
        }
    });
        JPanel mainPanel = new JPanel(new BorderLayout());
        getContentPane().add(mainPanel);

        JPanel controlPanel = new JPanel();
        mainPanel.add(controlPanel, BorderLayout.NORTH);

        controlPanel.add(new JLabel("Instrument:"));
        instrumentComboBox = new JComboBox<>();
        for (FinancialInstrument instrument : market.getInstruments()) {
            instrumentComboBox.addItem(instrument.getSymbol());
            XYSeries series = new XYSeries(instrument.getSymbol());
            seriesMap.put(instrument.getSymbol(), series);
            XYSeriesCollection dataset = new XYSeriesCollection(series);
            datasetMap.put(instrument.getSymbol(), dataset);
        }
        instrumentComboBox.addActionListener(e -> switchInstrument((String) instrumentComboBox.getSelectedItem()));
        controlPanel.add(instrumentComboBox);

        controlPanel.add(new JLabel("Quantity:"));
        quantityField = new JTextField(10);
        controlPanel.add(quantityField);

        JButton buyButton = new JButton("Buy");
        buyButton.addActionListener(e -> executeTrade(true));
        controlPanel.add(buyButton);

        JButton sellButton = new JButton("Sell");
        sellButton.addActionListener(e -> executeTrade(false));
        controlPanel.add(sellButton);

        currentPriceLabel = new JLabel("Current Price: ");
        controlPanel.add(currentPriceLabel);

        budgetLabel = new JLabel("Budget: $" + trader.getBudget());
        controlPanel.add(budgetLabel);

        portfolioTableModel = new DefaultTableModel(new Object[]{"Instrument", "Quantity"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable portfolioTable = new JTable(portfolioTableModel);
        JScrollPane portfolioScrollPane = new JScrollPane(portfolioTable);
        mainPanel.add(portfolioScrollPane, BorderLayout.CENTER);

        XYSeriesCollection initialDataset = datasetMap.get(currentInstrument);
        JFreeChart chart = ChartFactory.createXYLineChart("Price Over Time", "Time (seconds)", "Price", initialDataset, PlotOrientation.VERTICAL, true, true, false);
        chartPanel = new ChartPanel(chart);
        mainPanel.add(chartPanel, BorderLayout.SOUTH);

        updatePortfolioTable();
        switchInstrument(currentInstrument);
        reloadChartWithLoadedData(); // Load initial data to the chart
    }

    private void reloadChartWithLoadedData() {
        for (FinancialInstrument instrument : market.getInstruments()) {
            XYSeries series = seriesMap.get(instrument.getSymbol());
            if (series != null) {
                series.clear(); // Usuń stare dane
                long elapsedTime = simulator.getElapsedTime(); // Get the current elapsed time

                // Add the initial data points to the series
                series.add(elapsedTime / 1000.0, instrument.getCurrentPrice().doubleValue());
            }
        }

        // Revalidate and repaint the chart panel
        chartPanel.revalidate();
        chartPanel.repaint();
    }

    private void executeTrade(boolean isBuy) {
        try {
            BigDecimal quantity = new BigDecimal(quantityField.getText());
            FinancialInstrument instrument = market.getInstrument(currentInstrument);
            if (instrument != null) {
                boolean success = isBuy ? trader.buy(instrument, quantity) : trader.sell(instrument, quantity);
                if (success) {
                    SwingUtilities.invokeLater(this::updatePortfolioAndBudget);
                } else {
                    JOptionPane.showMessageDialog(this, "Insufficient funds or shares.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid quantity.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updatePortfolioAndBudget() {
        updatePortfolioTable();
        updateBudgetLabel();
    }

    private void updatePortfolioTable() {
        portfolioTableModel.setRowCount(0);
        for (Map.Entry<FinancialInstrument, BigDecimal> entry : trader.getPortfolio().getInstruments().entrySet()) {
            portfolioTableModel.addRow(new Object[]{entry.getKey().getName(), entry.getValue()});
        }
    }

    private void updateBudgetLabel() {
        budgetLabel.setText("Budget: $" + trader.getBudget());
    }

    @Override
    public void updateCurrentPrice(String symbol, BigDecimal price) {
        SwingUtilities.invokeLater(() -> {
            if (symbol.equals(currentInstrument)) {
                currentPriceLabel.setText("Current Price of " + symbol + ": " + price);
            }
        });
    }

    @Override
    public void updateChart(String symbol, BigDecimal price, long elapsedTime) {
        SwingUtilities.invokeLater(() -> {
            XYSeries series = seriesMap.get(symbol);
            if (series != null) {
                series.add(elapsedTime, price.doubleValue());
            }
        });
    }

    public void switchInstrument(String newInstrument) {
        synchronized (this) {
            this.currentInstrument = newInstrument;
            FinancialInstrument instrument = market.getInstrument(newInstrument);
            if (instrument != null) {
                XYSeriesCollection dataset = datasetMap.get(newInstrument);
                chartPanel.setChart(ChartFactory.createXYLineChart("Price Over Time", "Time (seconds)", "Price", dataset, PlotOrientation.VERTICAL, true, true, false));
                updateCurrentPrice(newInstrument, instrument.getCurrentPrice());
                reloadChartWithLoadedData(); // Add initial data points to the chart
            }
        }
    }

    public void setSimulator(MarketSimulator simulator) {
        this.simulator = simulator;
    }

    public Map<String, XYSeries> getSeriesMap() {
        return seriesMap;
    }
}
