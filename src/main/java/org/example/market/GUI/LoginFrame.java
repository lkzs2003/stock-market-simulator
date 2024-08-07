package org.example.market.GUI;

import org.example.market.model.Trader;
import org.example.market.model.Market;
import org.example.market.model.Currency;
import org.example.market.model.StockTrader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.util.Arrays;

public class LoginFrame extends JDialog {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private Trader authenticatedTrader;

    public LoginFrame(Frame parent) {
        super(parent, "Login", true);
        initialize();
    }

    private void initialize() {
        setSize(300, 200);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(getParent());

        JPanel panel = new JPanel(new GridLayout(4, 2));

        panel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        panel.add(usernameField);

        panel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        panel.add(passwordField);

        loginButton = new JButton("Login");
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                login();
            }
        });
        panel.add(loginButton);

        registerButton = new JButton("Register");
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                register();
            }
        });
        panel.add(registerButton);

        add(panel);
    }

    private void login() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        Trader trader = authenticate(username, password);
        if (trader != null) {
            authenticatedTrader = trader;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid credentials", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void register() {
        RegisterFrame registerFrame = new RegisterFrame((Frame) this.getParent());
        registerFrame.setVisible(true);
        Trader registeredTrader = registerFrame.getRegisteredTrader();
        if (registeredTrader != null) {
            JOptionPane.showMessageDialog(this, "Registration successful", "Success", JOptionPane.INFORMATION_MESSAGE);
            // Można tutaj dodać kod do zapisania nowego użytkownika do bazy danych lub listy użytkowników
        }
    }

    private Trader authenticate(String username, String password) {
        // Implementacja logiki uwierzytelniania użytkownika
        // Zwraca obiekt Trader, jeśli uwierzytelnianie się powiedzie, w przeciwnym razie zwraca null
        return new StockTrader("1", username, password, username + "@example.com", 5);
    }

    public Trader getAuthenticatedTrader() {
        return authenticatedTrader;
    }
}
