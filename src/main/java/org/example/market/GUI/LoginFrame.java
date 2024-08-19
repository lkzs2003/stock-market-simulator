package org.example.market.GUI;

import org.example.market.data.StockDataPoint;
import org.example.market.model.Market;
import org.example.market.model.Trader;
import org.example.market.service.UserService;
import org.example.market.simulation.MarketSimulator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private UserService userService;
    private Market market;
    private List<StockDataPoint> dataPoints;

    // Konstruktor przyjmujący trzy argumenty
    public LoginFrame(UserService userService, Market market, List<StockDataPoint> dataPoints) {
        this.userService = userService;
        this.market = market;
        this.dataPoints = dataPoints;
        initialize();
    }

    private void initialize() {
        setTitle("Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 200);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 2));

        panel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        panel.add(usernameField);

        panel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        panel.add(passwordField);

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(new LoginAction());
        panel.add(loginButton);

        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(new RegisterAction());
        panel.add(registerButton);

        add(panel);
    }

    private class LoginAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            Trader trader = userService.loginUser(username, password);
            if (trader != null) {
                JOptionPane.showMessageDialog(LoginFrame.this, "Login successful.");
                launchSimulation(trader);
            } else {
                JOptionPane.showMessageDialog(LoginFrame.this, "Login failed. Please check your credentials.");
            }
        }
    }

    private class RegisterAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            openRegisterFrame();
        }
    }

    private void openRegisterFrame() {
        SwingUtilities.invokeLater(() -> {
            // Tworzenie instancji RegisterFrame z trzema argumentami
            RegisterFrame registerFrame = new RegisterFrame(userService, market, dataPoints);
            registerFrame.setVisible(true);
            dispose(); // Zamknięcie okna logowania
        });
    }

    private void launchSimulation(Trader trader) {
        SwingUtilities.invokeLater(() -> {
            dispose();  // Zamknięcie okna logowania
            MarketSimulator simulator = new MarketSimulator(dataPoints, market, null);
            MarketGUI marketGUI = new MarketGUI(market, trader, simulator);
            simulator.setMarketGUI(marketGUI);
            marketGUI.show();
            simulator.startSimulation();
        });
    }
}
