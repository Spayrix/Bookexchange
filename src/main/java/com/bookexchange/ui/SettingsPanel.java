package com.bookexchange.ui;

import com.bookexchange.config.ConfigManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SettingsPanel extends JPanel {
    private JComboBox<String> dbTypeCombo;
    private JTextField hostField;
    private JTextField portField;
    private JTextField dbNameField;
    private JTextField userField;
    private JPasswordField passwordField;
    private JButton saveButton;
    private JButton resetButton;
    private ConfigManager configManager;

    public SettingsPanel() {
        this.configManager = ConfigManager.getInstance();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create title
        JLabel titleLabel = new JLabel("Database ayarı", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(titleLabel, BorderLayout.NORTH);

        // Create form panel
        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel dbTypeLabel = new JLabel("Veritabanı Türü:");
        dbTypeCombo = new JComboBox<>(new String[]{"mysql", "mongodb"});

        JLabel hostLabel = new JLabel("Host:");
        hostField = new JTextField(20);

        JLabel portLabel = new JLabel("Port:");
        portField = new JTextField(20);

        JLabel dbNameLabel = new JLabel("Veritabanı İsmi:");
        dbNameField = new JTextField(20);

        JLabel userLabel = new JLabel("Kullanıcı Adı:");
        userField = new JTextField(20);

        JLabel passwordLabel = new JLabel("Şifre:");
        passwordField = new JPasswordField(20);

        formPanel.add(dbTypeLabel);
        formPanel.add(dbTypeCombo);
        formPanel.add(hostLabel);
        formPanel.add(hostField);
        formPanel.add(portLabel);
        formPanel.add(portField);
        formPanel.add(dbNameLabel);
        formPanel.add(dbNameField);
        formPanel.add(userLabel);
        formPanel.add(userField);
        formPanel.add(passwordLabel);
        formPanel.add(passwordField);

        add(formPanel, BorderLayout.CENTER);

        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        saveButton = new JButton("Ayarı kaydet");
        resetButton = new JButton("Varsayılanlara Sıfırla");

        buttonPanel.add(saveButton);
        buttonPanel.add(resetButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // Add action listeners
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveConfiguration();
            }
        });

        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetConfiguration();
            }
        });

        // Load current configuration
        loadConfiguration();
    }

    private void loadConfiguration() {
        dbTypeCombo.setSelectedItem(configManager.getProperty(ConfigManager.DB_TYPE));
        hostField.setText(configManager.getProperty(ConfigManager.DB_HOST));
        portField.setText(configManager.getProperty(ConfigManager.DB_PORT));
        dbNameField.setText(configManager.getProperty(ConfigManager.DB_NAME));
        userField.setText(configManager.getProperty(ConfigManager.DB_USER));
        passwordField.setText(configManager.getProperty(ConfigManager.DB_PASSWORD));
    }

    private void saveConfiguration() {
        configManager.setProperty(ConfigManager.DB_TYPE, (String) dbTypeCombo.getSelectedItem());
        configManager.setProperty(ConfigManager.DB_HOST, hostField.getText());
        configManager.setProperty(ConfigManager.DB_PORT, portField.getText());
        configManager.setProperty(ConfigManager.DB_NAME, dbNameField.getText());
        configManager.setProperty(ConfigManager.DB_USER, userField.getText());
        configManager.setProperty(ConfigManager.DB_PASSWORD, new String(passwordField.getPassword()));

        configManager.saveConfig();

        JOptionPane.showMessageDialog(this, "Configuration saved successfully.\nPlease restart the application for changes to take effect.",
                "Configuration Saved", JOptionPane.INFORMATION_MESSAGE);
    }

    private void resetConfiguration() {
        int option = JOptionPane.showConfirmDialog(this,
                "Varsayılana sıfırlanmasını onaylıyor musunuz?",
                "Sıfırlamayı onayla", JOptionPane.YES_NO_OPTION);

        if (option == JOptionPane.YES_OPTION) {
            configManager.setProperty(ConfigManager.DB_TYPE, "mysql");
            configManager.setProperty(ConfigManager.DB_HOST, "localhost");
            configManager.setProperty(ConfigManager.DB_PORT, "3306");
            configManager.setProperty(ConfigManager.DB_NAME, "bookexchange");
            configManager.setProperty(ConfigManager.DB_USER, "root");
            configManager.setProperty(ConfigManager.DB_PASSWORD, "");

            configManager.saveConfig();
            loadConfiguration();

            JOptionPane.showMessageDialog(this, "Configuration reset to defaults.\nPlease restart the application for changes to take effect.",
                    "Configuration Reset", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}