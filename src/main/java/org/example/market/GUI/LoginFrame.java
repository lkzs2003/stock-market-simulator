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

            StockTrader trader = (StockTrader) userService.loginUser(username, password); // Zakładamy, że użytkownik jest typu StockTrader
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

    private void launchSimulation(StockTrader stockTrader) {
        SwingUtilities.invokeLater(() -> {
            dispose();  // Zamknięcie okna logowania
            MarketSimulator simulator = new MarketSimulator(dataPoints, market, stockTrader, null); // Tworzymy MarketSimulator
            MainFrame mainFrame = new MainFrame(stockTrader, market, simulator);  // Przekazujemy MarketSimulator do MainFrame
            simulator.setMarketGUI(mainFrame);
            mainFrame.setSimulator(simulator);
            mainFrame.setVisible(true);
            simulator.startSimulation();
        });
    }

}
