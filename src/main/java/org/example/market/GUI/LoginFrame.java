package org.example.market.GUI;

import org.example.market.data.StockDataPoint;
import org.example.market.model.Market;
import org.example.market.model.StockTrader;
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
    private final UserService userService;
    private final Market market;
    private final List<StockDataPoint> dataPoints;

    // Constructor that accepts UserService, Market, and List of StockDataPoints
    public LoginFrame(UserService userService, Market market, List<StockDataPoint> dataPoints) {
        this.userService = userService;
        this.market = market;
        this.dataPoints = dataPoints;
        initialize();
    }

    // Initialize the login frame
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

    // Action listener for the login button
    private class LoginAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            // Log in the user using the UserService
            StockTrader trader = (StockTrader) userService.loginUser(username, password);
            if (trader != null) {
                JOptionPane.showMessageDialog(LoginFrame.this, "Login successful.");
                launchSimulation(trader); // Launch the simulation with the logged-in StockTrader
            } else {
                JOptionPane.showMessageDialog(LoginFrame.this, "Login failed. Please check your credentials.");
            }
        }
    }

    // Action listener for the register button
    private class RegisterAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            openRegisterFrame();
        }
    }

    // Open the registration frame
    private void openRegisterFrame() {
        SwingUtilities.invokeLater(() -> {
            RegisterFrame registerFrame = new RegisterFrame(userService, market, dataPoints);
            registerFrame.setVisible(true);
            dispose(); // Close the login frame
        });
    }

    // Launch the simulation for the logged-in user
    private void launchSimulation(StockTrader stockTrader) {
        SwingUtilities.invokeLater(() -> {
            dispose();  // Close the login window
            MarketSimulator simulator = new MarketSimulator(dataPoints, market, stockTrader, null); // Pass null for GUI if not available
            MainFrame mainFrame = new MainFrame(stockTrader, market, simulator);  // Pass the simulator to MainFrame
            simulator.setMarketGUI(mainFrame);  // Connect the simulator to the GUI
            mainFrame.setSimulator(simulator);  // Connect the simulator
            mainFrame.setVisible(true);
            simulator.startSimulation();
        });
    }
}
