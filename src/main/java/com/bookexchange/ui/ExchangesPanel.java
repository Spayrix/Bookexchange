package com.bookexchange.ui;

import com.bookexchange.db.DatabaseManager;
import com.bookexchange.model.Exchange;
import com.bookexchange.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.List;

public class ExchangesPanel extends JPanel {
    private DatabaseManager dbManager;
    private User currentUser;
    private JTable exchangesTable;
    private DefaultTableModel tableModel;
    private JButton refreshButton;
    private SimpleDateFormat dateFormat;

    public ExchangesPanel(DatabaseManager dbManager, User currentUser) {
        this.dbManager = dbManager;
        this.currentUser = currentUser;
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        refreshButton = new JButton("Yenile");

        buttonPanel.add(refreshButton);

        add(buttonPanel, BorderLayout.NORTH);

        // Create table
        String[] columnNames = {"Kitap", "İsteyen", "Sağlayan", "Durum", "İstek Tarihi"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        exchangesTable = new JTable(tableModel);
        exchangesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        exchangesTable.setRowHeight(25);

        JScrollPane scrollPane = new JScrollPane(exchangesTable);
        add(scrollPane, BorderLayout.CENTER);

        // Add action listeners
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadExchanges();
            }
        });

        exchangesTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    viewExchangeDetails();
                }
            }
        });

        // Load exchanges
        loadExchanges();
    }

    private void loadExchanges() {
        // Clear table
        tableModel.setRowCount(0);

        // Get user's exchanges
        List<Exchange> exchanges = dbManager.getExchangesByUser(currentUser.getUsername());

        // Add exchanges to table
        for (Exchange exchange : exchanges) {
            Object[] row = {
                    exchange.getBookTitle(),
                    exchange.getRequesterName(),
                    exchange.getProviderName(),
                    translateStatus(exchange.getStatus()),
                    dateFormat.format(exchange.getRequestDate())
            };
            tableModel.addRow(row);
        }
    }

    private String translateStatus(String status) {
        switch (status) {
            case "PENDING": return "Beklemede";
            case "ACCEPTED": return "Kabul Edildi";
            case "REJECTED": return "Reddedildi";
            case "CANCELLED": return "İptal Edildi";
            case "COMPLETED": return "Tamamlandı";
            default: return status;
        }
    }

    private void viewExchangeDetails() {
        int selectedRow = exchangesTable.getSelectedRow();

        if (selectedRow == -1) {
            return;
        }

        String bookTitle = (String) tableModel.getValueAt(selectedRow, 0);
        String requesterName = (String) tableModel.getValueAt(selectedRow, 1);
        String providerName = (String) tableModel.getValueAt(selectedRow, 2);
        String status = (String) tableModel.getValueAt(selectedRow, 3);

        // Find the exchange in the database
        List<Exchange> exchanges = dbManager.getExchangesByUser(currentUser.getUsername());
        Exchange selectedExchange = null;

        for (Exchange exchange : exchanges) {
            if (exchange.getBookTitle().equals(bookTitle) &&
                    exchange.getRequesterName().equals(requesterName) &&
                    exchange.getProviderName().equals(providerName) &&
                    translateStatus(exchange.getStatus()).equals(status)) {
                selectedExchange = exchange;
                break;
            }
        }

        if (selectedExchange == null) {
            return;
        }

        // Create dialog
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Takas Detayları", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Details panel
        JPanel detailsPanel = new JPanel(new GridLayout(5, 2, 5, 5));

        detailsPanel.add(new JLabel("Kitap:"));
        detailsPanel.add(new JLabel(selectedExchange.getBookTitle()));

        detailsPanel.add(new JLabel("İsteyen:"));
        detailsPanel.add(new JLabel(selectedExchange.getRequesterName()));

        detailsPanel.add(new JLabel("Sağlayan:"));
        detailsPanel.add(new JLabel(selectedExchange.getProviderName()));

        detailsPanel.add(new JLabel("Durum:"));
        detailsPanel.add(new JLabel(translateStatus(selectedExchange.getStatus())));

        detailsPanel.add(new JLabel("İstek Tarihi:"));
        detailsPanel.add(new JLabel(dateFormat.format(selectedExchange.getRequestDate())));

        panel.add(detailsPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton closeButton = new JButton("Kapat");
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });

        buttonPanel.add(closeButton);

        // Add action buttons based on exchange status and user role
        final Exchange finalSelectedExchange = selectedExchange; // Make it final for inner classes

        if ("PENDING".equals(finalSelectedExchange.getStatus())) {
            if (currentUser.getUsername().equals(finalSelectedExchange.getProviderName())) {
                // Provider can accept or reject
                JButton acceptButton = new JButton("Kabul Et");
                JButton rejectButton = new JButton("Reddet");

                acceptButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        updateExchangeStatus(finalSelectedExchange, "ACCEPTED");
                        dialog.dispose();
                    }
                });

                rejectButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        updateExchangeStatus(finalSelectedExchange, "REJECTED");
                        dialog.dispose();
                    }
                });

                buttonPanel.add(acceptButton);
                buttonPanel.add(rejectButton);
            } else {
                // Requester can cancel
                JButton cancelButton = new JButton("İsteği İptal Et");

                cancelButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        updateExchangeStatus(finalSelectedExchange, "CANCELLED");
                        dialog.dispose();
                    }
                });

                buttonPanel.add(cancelButton);
            }
        } else if ("ACCEPTED".equals(finalSelectedExchange.getStatus())) {
            // Both users can mark as completed
            JButton completeButton = new JButton("Tamamlandı Olarak İşaretle");

            completeButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    updateExchangeStatus(finalSelectedExchange, "COMPLETED");
                    dialog.dispose();
                }
            });

            buttonPanel.add(completeButton);
        }

        panel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void updateExchangeStatus(Exchange exchange, String status) {
        if (dbManager.updateExchangeStatus(exchange.getId(), status)) {
            JOptionPane.showMessageDialog(this, "Takas durumu başarıyla güncellendi",
                    "Başarılı", JOptionPane.INFORMATION_MESSAGE);
            loadExchanges();
        } else {
            JOptionPane.showMessageDialog(this, "Takas durumu güncellenirken hata oluştu",
                    "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }
}
