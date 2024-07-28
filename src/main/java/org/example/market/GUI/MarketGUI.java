package org.example.market.GUI;

import org.example.market.model.*;
import org.example.market.simulation.MarketSimulator;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MarketGUI {
    private JFrame frame;
    private JComboBox<String> instrumentComboBox;
    private JTextField quantityField;
    private JLabel currentPriceLabel;
    private JLabel budgetLabel;
    private JTable portfolioTable;
    private DefaultTableModel portfolioTableModel;
    private final Market market;
    private final Trader trader;
    private MarketSimulator simulator;
    private final Map<String, XYSeries> seriesMap;
    private final Map<String, XYSeriesCollection> datasetMap;
    private ChartPanel chartPanel;
    private String currentInstrument;

    public MarketGUI(Market market, Trader trader, MarketSimulator simulator) {
        this.market = market;
        this.trader = trader;
        this.simulator = simulator;
        this.seriesMap = new ConcurrentHashMap<>();
        this.datasetMap = new ConcurrentHashMap<>();
        this.currentInstrument = market.getInstruments().getFirst().getSymbol(); // Set first available instrument
        initialize();
    }

    public void setSimulator(MarketSimulator simulator) {
        this.simulator = simulator;
    }

    private void initialize() {
        frame = new JFrame("Stock Market Simulator");
        frame.setBounds(100, 100, 800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout(0, 0));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        frame.getContentPane().add(mainPanel, BorderLayout.CENTER);

        JPanel panel = new JPanel();
        frame.getContentPane().add(panel, BorderLayout.NORTH);

        JLabel lblInstrument = new JLabel("Instrument:");
        panel.add(lblInstrument);

        instrumentComboBox = new JComboBox<>();
        for (FinancialInstrument instrument : market.getInstruments()) {
            instrumentComboBox.addItem(instrument.getSymbol());
            XYSeries series = new XYSeries(instrument.getSymbol());
            seriesMap.put(instrument.getSymbol(), series);
            XYSeriesCollection dataset = new XYSeriesCollection(series);
            datasetMap.put(instrument.getSymbol(), dataset);
        }
        instrumentComboBox.addActionListener(e -> {
            String symbol = (String) instrumentComboBox.getSelectedItem();
            switchInstrument(symbol);
        });
        panel.add(instrumentComboBox);

        JLabel lblQuantity = new JLabel("Quantity:");
        panel.add(lblQuantity);

        quantityField = new JTextField();
        panel.add(quantityField);
        quantityField.setColumns(10);

        JButton btnBuy = new JButton("Buy");
        btnBuy.addActionListener(e -> {
            BigDecimal quantity = new BigDecimal(quantityField.getText());
            FinancialInstrument instrument = market.getInstrument(currentInstrument);
            if (instrument != null) {
                boolean success = trader.buy(instrument, quantity);
                if (success) {
                    SwingUtilities.invokeLater(() -> {
                        updatePortfolioTable();
                        updateBudgetLabel();
                    });
                } else {
                    JOptionPane.showMessageDialog(frame, "Insufficient funds to buy.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        panel.add(btnBuy);

        JButton btnSell = new JButton("Sell");
        btnSell.addActionListener(e -> {
            BigDecimal quantity = new BigDecimal(quantityField.getText());
            FinancialInstrument instrument = market.getInstrument(currentInstrument);
            if (instrument != null) {
                boolean success = trader.sell(instrument, quantity);
                if (success) {
                    SwingUtilities.invokeLater(() -> {
                        updatePortfolioTable();
                        updateBudgetLabel();
                    });
                } else {
                    JOptionPane.showMessageDialog(frame, "Insufficient shares to sell.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        panel.add(btnSell);

        currentPriceLabel = new JLabel("Current Price: ");
        panel.add(currentPriceLabel);

        budgetLabel = new JLabel("Budget: $" + trader.getBudget());
        panel.add(budgetLabel);

        portfolioTableModel = new DefaultTableModel(new Object[]{"Instrument", "Quantity"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        portfolioTable = new JTable(portfolioTableModel);
        JScrollPane portfolioScrollPane = new JScrollPane(portfolioTable);
        mainPanel.add(portfolioScrollPane, BorderLayout.CENTER);

        // Initialize the chart with the default instrument's dataset
        XYSeriesCollection initialDataset = datasetMap.get(currentInstrument);
        JFreeChart chart = ChartFactory.createXYLineChart("Price Over Time", "Time (seconds)", "Price", initialDataset, PlotOrientation.VERTICAL, true, true, false);
        XYPlot plot = (XYPlot) chart.getPlot();
        NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
        domainAxis.setAutoRangeIncludesZero(false);
        chartPanel = new ChartPanel(chart);
        mainPanel.add(chartPanel, BorderLayout.SOUTH);

        updatePortfolioTable();
        switchInstrument(currentInstrument); // Set the default instrument

        // Set up a timer to update the chart
        Timer updateTimer = new Timer(1000, e -> simulator.updateCurrentInstrumentData(currentInstrument));
        updateTimer.start();
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

    public void show() {
        frame.setVisible(true);
    }

    public void updateCurrentPrice(String symbol, BigDecimal price) {
        SwingUtilities.invokeLater(() -> {
            if (symbol.equals(currentInstrument)) {
                currentPriceLabel.setText("Current Price of " + symbol + ": " + price);
            }
        });
    }

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
                System.out.println("Switched to instrument: " + newInstrument); // Debugging line
                XYSeriesCollection dataset = datasetMap.get(newInstrument);
                chartPanel.setChart(ChartFactory.createXYLineChart("Price Over Time", "Time (seconds)", "Price", dataset, PlotOrientation.VERTICAL, true, true, false));
                updateCurrentPrice(newInstrument, instrument.getCurrentPrice());
            }
        }
    }

    public String getCurrentInstrument() {
        return currentInstrument;
    }
}

