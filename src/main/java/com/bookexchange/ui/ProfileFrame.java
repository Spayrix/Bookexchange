package com.bookexchange.ui;

import com.bookexchange.db.DatabaseManager;
import com.bookexchange.model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ProfileFrame extends JDialog {
    private DatabaseManager dbManager;
    private User currentUser;
    private JTextField emailField;
    private JTextField fullNameField;
    private JTextArea addressArea;
    private JButton saveButton;
    private JButton cancelButton;

    public ProfileFrame(DatabaseManager dbManager, User currentUser) {
        super((Frame) null, "Edit Profile", true);
        this.dbManager = dbManager;
        this.currentUser = currentUser;

        setSize(400, 350);
        setLocationRelativeTo(null);

        // Create components
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title
        JLabel titleLabel = new JLabel("Edit Your Profile", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Form panel
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));

        JLabel usernameLabel = new JLabel("Username:");
        JLabel usernameValue = new JLabel(currentUser.getUsername());
        usernameValue.setFont(new Font("Arial", Font.BOLD, 12));

        JLabel emailLabel = new JLabel("Email:");
        emailField = new JTextField(currentUser.getEmail(), 20);

        JLabel fullNameLabel = new JLabel("Full Name:");
        fullNameField = new JTextField(currentUser.getFullName(), 20);

        JLabel addressLabel = new JLabel("Address:");
        addressArea = new JTextArea(currentUser.getAddress(), 4, 20);
        JScrollPane addressScrollPane = new JScrollPane(addressArea);

        formPanel.add(usernameLabel);
        formPanel.add(usernameValue);
        formPanel.add(emailLabel);
        formPanel.add(emailField);
        formPanel.add(fullNameLabel);
        formPanel.add(fullNameField);
        formPanel.add(addressLabel);
        formPanel.add(addressScrollPane);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        saveButton = new JButton("Save Changes");
        cancelButton = new JButton("Cancel");

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Add action listeners
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveProfile();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        // Add main panel to dialog
        add(mainPanel);
    }

    private void saveProfile() {
        String email = emailField.getText();
        String fullName = fullNameField.getText();
        String address = addressArea.getText();

        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Email is required",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        currentUser.setEmail(email);
        currentUser.setFullName(fullName);
        currentUser.setAddress(address);

        if (dbManager.updateUser(currentUser)) {
            JOptionPane.showMessageDialog(this, "Profile updated successfully",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update profile",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}