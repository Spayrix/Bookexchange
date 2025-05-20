package com.bookexchange.ui;

import com.bookexchange.db.DatabaseManager;
import com.bookexchange.model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RegisterFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField emailField;
    private JTextField fullNameField;
    private JTextArea addressArea;
    private JButton registerButton;
    private JButton backButton;
    private DatabaseManager dbManager;

    public RegisterFrame(DatabaseManager dbManager) {
        this.dbManager = dbManager;

        setTitle("Kitap Takası - Kayıt");
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create components
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title
        JLabel titleLabel = new JLabel("Yeni Kullanıcı Kaydı", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Registration form
        JPanel formPanel = new JPanel(new GridLayout(7, 2, 10, 10));

        JLabel usernameLabel = new JLabel("Kullanıcı Adı:");
        usernameField = new JTextField(20);

        JLabel passwordLabel = new JLabel("Şifre:");
        passwordField = new JPasswordField(20);

        JLabel confirmPasswordLabel = new JLabel("Şifre Tekrar:");
        confirmPasswordField = new JPasswordField(20);

        JLabel emailLabel = new JLabel("E-posta:");
        emailField = new JTextField(20);

        JLabel fullNameLabel = new JLabel("Ad Soyad:");
        fullNameField = new JTextField(20);

        JLabel addressLabel = new JLabel("Adres:");
        addressArea = new JTextArea(4, 20);
        JScrollPane addressScrollPane = new JScrollPane(addressArea);

        registerButton = new JButton("Kayıt Ol");
        backButton = new JButton("Geri");

        formPanel.add(usernameLabel);
        formPanel.add(usernameField);
        formPanel.add(passwordLabel);
        formPanel.add(passwordField);
        formPanel.add(confirmPasswordLabel);
        formPanel.add(confirmPasswordField);
        formPanel.add(emailLabel);
        formPanel.add(emailField);
        formPanel.add(fullNameLabel);
        formPanel.add(fullNameField);
        formPanel.add(addressLabel);
        formPanel.add(addressScrollPane);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.add(registerButton);
        buttonPanel.add(backButton);

        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Add action listeners
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                register();
            }
        });

        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                goBack();
            }
        });

        // Add main panel to frame
        add(mainPanel);
    }

    private void register() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        String email = emailField.getText();
        String fullName = fullNameField.getText();
        String address = addressArea.getText();

        // Validate input
        if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Kullanıcı adı, şifre ve e-posta zorunludur",
                    "Kayıt Hatası", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Şifreler eşleşmiyor",
                    "Kayıt Hatası", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Create user
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.setFullName(fullName);
        user.setAddress(address);

        // Register user
        try {
            if (dbManager.registerUser(user)) {
                JOptionPane.showMessageDialog(this, "Kayıt başarılı! Şimdi giriş yapabilirsiniz.",
                        "Kayıt Başarılı", JOptionPane.INFORMATION_MESSAGE);
                goBack();
            } else {
                JOptionPane.showMessageDialog(this, "Kayıt başarısız. Kullanıcı adı veya e-posta zaten kullanılıyor olabilir.",
                        "Kayıt Hatası", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Kayıt sırasında hata: " + e.getMessage(),
                    "Kayıt Hatası",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void goBack() {
        LoginFrame loginFrame = new LoginFrame();
        loginFrame.setVisible(true);
        dispose();
    }
}
