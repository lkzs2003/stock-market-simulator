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
        this.currentInstrument = market.getInstruments().get(0).getSymbol();
        initialize();
    }

    private void initialize() {
        frame = createMainFrame();
        JPanel mainPanel = createMainPanel();
        JPanel controlPanel = createControlPanel();

        mainPanel.add(controlPanel, BorderLayout.NORTH);
        mainPanel.add(createPortfolioScrollPane(), BorderLayout.CENTER);
        mainPanel.add(createChartPanel(), BorderLayout.SOUTH);

        frame.getContentPane().add(mainPanel);
        frame.setVisible(true);
    }

    private JFrame createMainFrame() {
        JFrame frame = new JFrame("Stock Market Simulator");
        frame.setBounds(100, 100, 800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        return frame;
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        return mainPanel;
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.add(new JLabel("Instrument:"));
        panel.add(createInstrumentComboBox());
        panel.add(new JLabel("Quantity:"));
        panel.add(createQuantityField());
        panel.add(createBuyButton());
        panel.add(createSellButton());
        panel.add(currentPriceLabel = new JLabel("Current Price: "));
        panel.add(budgetLabel = new JLabel("Budget: $" + trader.getBudget()));
        return panel;
    }

    private JComboBox<String> createInstrumentComboBox() {
        instrumentComboBox = new JComboBox<>();
        market.getInstruments().forEach(instrument -> {
            instrumentComboBox.addItem(instrument.getSymbol());
            XYSeries series = new XYSeries(instrument.getSymbol());
            seriesMap.put(instrument.getSymbol(), series);
            datasetMap.put(instrument.getSymbol(), new XYSeriesCollection(series));
        });
        instrumentComboBox.addActionListener(e -> switchInstrument((String) instrumentComboBox.getSelectedItem()));
        return instrumentComboBox;
    }

    private JTextField createQuantityField() {
        quantityField = new JTextField(10);
        return quantityField;
    }

    private JButton createBuyButton() {
        JButton btnBuy = new JButton("Buy");
        btnBuy.addActionListener(e -> executeTrade(true));
        return btnBuy;
    }

    private JButton createSellButton() {
        JButton btnSell = new JButton("Sell");
        btnSell.addActionListener(e -> executeTrade(false));
        return btnSell;
    }

    private void executeTrade(boolean isBuy) {
        BigDecimal quantity = new BigDecimal(quantityField.getText());
        FinancialInstrument instrument = market.getInstrument(currentInstrument);
        if (instrument != null) {
            boolean success = isBuy ? trader.buy(instrument, quantity) : trader.sell(instrument, quantity);
            if (success) {
                SwingUtilities.invokeLater(this::updatePortfolioAndBudget);
            } else {
                JOptionPane.showMessageDialog(frame, "Insufficient funds or shares.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JScrollPane createPortfolioScrollPane() {
        portfolioTableModel = new DefaultTableModel(new Object[]{"Instrument", "Quantity"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        portfolioTable = new JTable(portfolioTableModel);
        return new JScrollPane(portfolioTable);
    }

    private ChartPanel createChartPanel() {
        XYSeriesCollection initialDataset = datasetMap.get(currentInstrument);
        JFreeChart chart = ChartFactory.createXYLineChart("Price Over Time", "Time (seconds)", "Price", initialDataset, PlotOrientation.VERTICAL, true, true, false);
        chartPanel = new ChartPanel(chart);
        return chartPanel;
    }

    private void updatePortfolioAndBudget() {
        updatePortfolioTable();
        updateBudgetLabel();
    }

    private void updatePortfolioTable() {
        portfolioTableModel.setRowCount(0);
        trader.getPortfolio().getInstruments().forEach((instrument, quantity) -> {
            portfolioTableModel.addRow(new Object[]{instrument.getName(), quantity});
        });
    }

    private void updateBudgetLabel() {
        budgetLabel.setText("Budget: $" + trader.getBudget());
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
        currentInstrument = newInstrument;
        FinancialInstrument instrument = market.getInstrument(newInstrument);
        if (instrument != null) {
            chartPanel.setChart(ChartFactory.createXYLineChart("Price Over Time", "Time (seconds)", "Price", datasetMap.get(newInstrument), PlotOrientation.VERTICAL, true, true, false));
            updateCurrentPrice(newInstrument, instrument.getCurrentPrice());
        }
    }

    public void show() {
        frame.setVisible(true);
    }

    public String getCurrentInstrument() {
        return currentInstrument;
    }
}
