package com.bookexchange.ui;

import com.bookexchange.db.DatabaseFactory;
import com.bookexchange.db.DatabaseManager;
import com.bookexchange.model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private DatabaseManager dbManager;

    public LoginFrame() {
        setTitle("Kitap Takası - Giriş");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Initialize database connection
        try {
            dbManager = DatabaseFactory.getDatabaseManager();
            dbManager.connect();

            if (!dbManager.isConnected()) {
                JOptionPane.showMessageDialog(this,
                        "Veritabanına bağlanılamadı. Lütfen veritabanı ayarlarınızı kontrol edin.",
                        "Veritabanı Bağlantı Hatası",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Veritabanına bağlanırken hata: " + e.getMessage(),
                    "Veritabanı Bağlantı Hatası",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        // Create components
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title
        JLabel titleLabel = new JLabel("Kitap Takası Sistemi", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Login form
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));

        JLabel usernameLabel = new JLabel("Kullanıcı Adı:");
        usernameField = new JTextField(20);

        JLabel passwordLabel = new JLabel("Şifre:");
        passwordField = new JPasswordField(20);

        loginButton = new JButton("Giriş Yap");
        registerButton = new JButton("Kayıt Ol");

        formPanel.add(usernameLabel);
        formPanel.add(usernameField);
        formPanel.add(passwordLabel);
        formPanel.add(passwordField);
        formPanel.add(loginButton);
        formPanel.add(registerButton);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Add action listeners
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                login();
            }
        });

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openRegisterFrame();
            }
        });

        // Add main panel to frame
        add(mainPanel);
    }

    private void login() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Lütfen kullanıcı adı ve şifre girin",
                    "Giriş Hatası", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!dbManager.isConnected()) {
            try {
                dbManager.connect();
                if (!dbManager.isConnected()) {
                    JOptionPane.showMessageDialog(this,
                            "Veritabanına bağlanılamıyor. Lütfen ayarlarınızı kontrol edin.",
                            "Veritabanı Hatası",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Veritabanına bağlanırken hata: " + e.getMessage(),
                        "Veritabanı Hatası",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                return;
            }
        }

        try {
            if (dbManager.authenticateUser(username, password)) {
                User user = dbManager.getUserByUsername(username);
                MainFrame mainFrame = new MainFrame(user);
                mainFrame.setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Geçersiz kullanıcı adı veya şifre",
                        "Giriş Hatası", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Giriş sırasında hata: " + e.getMessage(),
                    "Giriş Hatası",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void openRegisterFrame() {
        RegisterFrame registerFrame = new RegisterFrame(dbManager);
        registerFrame.setVisible(true);
        dispose();
    }
}
