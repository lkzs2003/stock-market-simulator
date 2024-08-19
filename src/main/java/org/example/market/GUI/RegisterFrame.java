package org.example.market.GUI;

import org.example.market.data.StockDataPoint;
import org.example.market.model.Market;
import org.example.market.service.UserService;
import org.example.market.model.Trader;
import org.example.market.model.StockTrader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class RegisterFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private UserService userService;
    private Market market;
    private List<StockDataPoint> dataPoints;

    // Konstruktor przyjmujący trzy argumenty
    public RegisterFrame(UserService userService, Market market, List<StockDataPoint> dataPoints) {
        this.userService = userService;
        this.market = market;
        this.dataPoints = dataPoints;
        initialize();
    }

    private void initialize() {
        setTitle("Register");
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

        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(new RegisterAction());
        panel.add(registerButton);

        JButton loginButton = new JButton("Back to Login");
        loginButton.addActionListener(new LoginAction());
        panel.add(loginButton);

        add(panel);
    }

    private class RegisterAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            Trader trader = new StockTrader("1", username, password, username + "@example.com", 5);

            if (userService.registerUser(trader)) {
                JOptionPane.showMessageDialog(RegisterFrame.this, "Registration successful. You can now log in.");
                openLoginFrame();
            } else {
                JOptionPane.showMessageDialog(RegisterFrame.this, "User already exists.");
            }
        }
    }

    private class LoginAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            openLoginFrame();
        }
    }

    private void openLoginFrame() {
        SwingUtilities.invokeLater(() -> {
            // Przekazanie wszystkich wymaganych argumentów do LoginFrame
            LoginFrame loginFrame = new LoginFrame(userService, market, dataPoints);
            loginFrame.setVisible(true);
            dispose(); // Zamknięcie okna rejestracji
        });
    }
}
