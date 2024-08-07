package org.example.market.GUI;

import org.example.market.model.*;
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
    private Trader trader;
    private Market market;
    private MarketSimulator simulator;
    private JComboBox<String> instrumentComboBox;
    private JTextField quantityField;
    private JLabel currentPriceLabel;
    private JLabel budgetLabel;
    private JTable portfolioTable;
    private DefaultTableModel portfolioTableModel;
    private Map<String, XYSeries> seriesMap;
    private Map<String, XYSeriesCollection> datasetMap;
    private ChartPanel chartPanel;
    private String currentInstrument;

    public MainFrame(Trader trader, Market market) {
        this.trader = trader;
        this.market = market;
        this.seriesMap = new ConcurrentHashMap<>();
        this.datasetMap = new ConcurrentHashMap<>();
        this.currentInstrument = market.getInstruments().get(0).getSymbol(); // Set first available instrument
        initialize();
    }

    private void initialize() {
        setTitle("Stock Market Simulator");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

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
        portfolioTable = new JTable(portfolioTableModel);
        JScrollPane portfolioScrollPane = new JScrollPane(portfolioTable);
        mainPanel.add(portfolioScrollPane, BorderLayout.CENTER);

        XYSeriesCollection initialDataset = datasetMap.get(currentInstrument);
        JFreeChart chart = ChartFactory.createXYLineChart("Price Over Time", "Time (seconds)", "Price", initialDataset, PlotOrientation.VERTICAL, true, true, false);
        chartPanel = new ChartPanel(chart);
        mainPanel.add(chartPanel, BorderLayout.SOUTH);

        updatePortfolioTable();
        switchInstrument(currentInstrument);

        Timer updateTimer = new Timer(1000, e -> {
            if (simulator != null) {
                simulator.updateCurrentInstrumentData(currentInstrument);
            }
        });
        updateTimer.start();
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
            }
        }
    }

    @Override
    public String getCurrentInstrument() {
        return currentInstrument;
    }

    public void setSimulator(MarketSimulator simulator) {
        this.simulator = simulator;
    }
}
