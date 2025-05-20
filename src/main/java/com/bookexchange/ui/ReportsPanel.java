package com.bookexchange.ui;

import com.bookexchange.db.DatabaseManager;
import com.bookexchange.model.Book;
import com.bookexchange.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class ReportsPanel extends JPanel {
    private DatabaseManager dbManager;
    private JTabbedPane reportsTabbedPane;
    private JTable mostExchangedBooksTable;
    private DefaultTableModel mostExchangedBooksModel;
    private JTable mostActiveUsersTable;
    private DefaultTableModel mostActiveUsersModel;
    private JPanel statsPanel;
    private JButton refreshButton;

    public ReportsPanel(DatabaseManager dbManager) {
        this.dbManager = dbManager;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        refreshButton = new JButton("Raporları Yenile");

        buttonPanel.add(refreshButton);

        add(buttonPanel, BorderLayout.NORTH);

        // Create tabbed pane for reports
        reportsTabbedPane = new JTabbedPane();

        // Create most exchanged books panel
        JPanel mostExchangedBooksPanel = createMostExchangedBooksPanel();
        reportsTabbedPane.addTab("En Çok Takas Edilen Kitaplar", mostExchangedBooksPanel);

        // Create most active users panel
        JPanel mostActiveUsersPanel = createMostActiveUsersPanel();
        reportsTabbedPane.addTab("En Aktif Kullanıcılar", mostActiveUsersPanel);

        // Create statistics panel
        statsPanel = createStatsPanel();
        reportsTabbedPane.addTab("İstatistikler", statsPanel);

        add(reportsTabbedPane, BorderLayout.CENTER);

        // Add action listeners
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshReports();
            }
        });

        // Load reports
        loadReports();
    }

    private JPanel createMostExchangedBooksPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        String[] columnNames = {"Başlık", "Yazar", "Sahibi", "Takas Sayısı"};
        mostExchangedBooksModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        mostExchangedBooksTable = new JTable(mostExchangedBooksModel);
        mostExchangedBooksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        mostExchangedBooksTable.setRowHeight(25);

        JScrollPane scrollPane = new JScrollPane(mostExchangedBooksTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createMostActiveUsersPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        String[] columnNames = {"Kullanıcı Adı", "Ad Soyad", "Takas Sayısı"};
        mostActiveUsersModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        mostActiveUsersTable = new JTable(mostActiveUsersModel);
        mostActiveUsersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        mostActiveUsersTable.setRowHeight(25);

        JScrollPane scrollPane = new JScrollPane(mostActiveUsersTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        return panel;
    }

    private void loadReports() {
        // Load most exchanged books
        loadMostExchangedBooks();

        // Load most active users
        loadMostActiveUsers();

        // Load statistics
        loadStats();
    }

    private void loadMostExchangedBooks() {
        // Clear table
        mostExchangedBooksModel.setRowCount(0);

        // Get most exchanged books
        List<Book> books = dbManager.getMostExchangedBooks(10);

        // Add books to table
        for (Book book : books) {
            Object[] row = {
                    book.getTitle(),
                    book.getAuthor(),
                    book.getOwnerName(),
                    book.getExchangeCount()
            };
            mostExchangedBooksModel.addRow(row);
        }
    }

    private void loadMostActiveUsers() {
        // Clear table
        mostActiveUsersModel.setRowCount(0);

        // Get most active users
        List<User> users = dbManager.getMostActiveUsers(10);

        // Add users to table
        for (User user : users) {
            Object[] row = {
                    user.getUsername(),
                    user.getFullName(),
                    user.getExchangeCount()
            };
            mostActiveUsersModel.addRow(row);
        }
    }

    private void loadStats() {
        // Clear panel
        statsPanel.removeAll();

        // Get statistics
        int totalBooks = dbManager.getTotalBooks();
        int totalUsers = dbManager.getTotalUsers();
        int totalExchanges = dbManager.getTotalExchanges();

        // Add statistics to panel
        statsPanel.add(new JLabel("Toplam Kitap Sayısı:"));
        statsPanel.add(new JLabel(String.valueOf(totalBooks), JLabel.RIGHT));

        statsPanel.add(new JLabel("Toplam Kullanıcı Sayısı:"));
        statsPanel.add(new JLabel(String.valueOf(totalUsers), JLabel.RIGHT));

        statsPanel.add(new JLabel("Toplam Takas Sayısı:"));
        statsPanel.add(new JLabel(String.valueOf(totalExchanges), JLabel.RIGHT));

        // Refresh panel
        statsPanel.revalidate();
        statsPanel.repaint();
    }

    private void refreshReports() {
        loadReports();
    }
}
