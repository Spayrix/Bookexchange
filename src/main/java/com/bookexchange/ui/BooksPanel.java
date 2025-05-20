package com.bookexchange.ui;

import com.bookexchange.db.DatabaseManager;
import com.bookexchange.model.Book;
import com.bookexchange.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class BooksPanel extends JPanel {
    private DatabaseManager dbManager;
    private User currentUser;
    private JTable booksTable;
    private DefaultTableModel tableModel;
    private JButton refreshButton;
    private JButton requestButton;

    public BooksPanel(DatabaseManager dbManager, User currentUser) {
        this.dbManager = dbManager;
        this.currentUser = currentUser;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        refreshButton = new JButton("Yenile");
        requestButton = new JButton("Kitap İste");

        buttonPanel.add(refreshButton);
        buttonPanel.add(requestButton);

        add(buttonPanel, BorderLayout.NORTH);

        // Create table
        String[] columnNames = {"Başlık", "Yazar", "ISBN", "Durum", "Sahibi"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        booksTable = new JTable(tableModel);
        booksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        booksTable.setRowHeight(25);

        JScrollPane scrollPane = new JScrollPane(booksTable);
        add(scrollPane, BorderLayout.CENTER);

        // Add action listeners
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadBooks();
            }
        });

        requestButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                requestBook();
            }
        });

        booksTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    viewBookDetails();
                }
            }
        });

        // Load books
        loadBooks();
    }

    private void loadBooks() {
        // Clear table
        tableModel.setRowCount(0);

        // Get all available books
        List<Book> books = dbManager.getAllBooks();
        System.out.println("Yüklenen mevcut kitap sayısı: " + books.size());

        // Add books to table
        for (Book book : books) {
            // Don't show the current user's books
            if (!book.getOwnerId().equals(currentUser.getId())) {
                Object[] row = {
                        book.getTitle(),
                        book.getAuthor(),
                        book.getIsbn(),
                        book.getCondition(),
                        book.getOwnerName()
                };
                tableModel.addRow(row);
                System.out.println("Tabloya eklendi: " + book.getTitle() + " - " + book.getAuthor() + " (Sahibi: " + book.getOwnerName() + ")");
            }
        }
    }

    private void viewBookDetails() {
        int selectedRow = booksTable.getSelectedRow();

        if (selectedRow == -1) {
            return;
        }

        String title = (String) tableModel.getValueAt(selectedRow, 0);
        String author = (String) tableModel.getValueAt(selectedRow, 1);
        String isbn = (String) tableModel.getValueAt(selectedRow, 2);
        String condition = (String) tableModel.getValueAt(selectedRow, 3);
        String ownerName = (String) tableModel.getValueAt(selectedRow, 4);

        // Find the book in the database
        List<Book> books = dbManager.getAllBooks();
        Book selectedBook = null;

        for (Book book : books) {
            if (book.getTitle().equals(title) &&
                    book.getAuthor().equals(author) &&
                    book.getOwnerName().equals(ownerName)) {
                selectedBook = book;
                break;
            }
        }

        if (selectedBook == null) {
            return;
        }

        // Create dialog
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Kitap Detayları", true);
        dialog.setSize(400, 400);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Details panel
        JPanel detailsPanel = new JPanel(new GridLayout(6, 2, 5, 5));

        detailsPanel.add(new JLabel("Başlık:"));
        detailsPanel.add(new JLabel(selectedBook.getTitle()));

        detailsPanel.add(new JLabel("Yazar:"));
        detailsPanel.add(new JLabel(selectedBook.getAuthor()));

        detailsPanel.add(new JLabel("ISBN:"));
        detailsPanel.add(new JLabel(selectedBook.getIsbn() != null ? selectedBook.getIsbn() : ""));

        detailsPanel.add(new JLabel("Durum:"));
        detailsPanel.add(new JLabel(selectedBook.getCondition()));

        detailsPanel.add(new JLabel("Sahibi:"));
        detailsPanel.add(new JLabel(selectedBook.getOwnerName()));

        detailsPanel.add(new JLabel("Açıklama:"));
        JTextArea descriptionArea = new JTextArea(selectedBook.getDescription());
        descriptionArea.setEditable(false);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane descriptionScrollPane = new JScrollPane(descriptionArea);

        panel.add(detailsPanel, BorderLayout.NORTH);
        panel.add(descriptionScrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton closeButton = new JButton("Kapat");
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });

        JButton requestButton = new JButton("Takas İste");
        Book finalSelectedBook = selectedBook;
        requestButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                requestExchange(finalSelectedBook);
                dialog.dispose();
            }
        });

        buttonPanel.add(requestButton);
        buttonPanel.add(closeButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void requestBook() {
        int selectedRow = booksTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Lütfen istemek için bir kitap seçin",
                    "Seçim Gerekli", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String title = (String) tableModel.getValueAt(selectedRow, 0);
        String author = (String) tableModel.getValueAt(selectedRow, 1);
        String ownerName = (String) tableModel.getValueAt(selectedRow, 4);

        // Find the book in the database
        List<Book> books = dbManager.getAllBooks();
        Book selectedBook = null;

        for (Book book : books) {
            if (book.getTitle().equals(title) &&
                    book.getAuthor().equals(author) &&
                    book.getOwnerName().equals(ownerName)) {
                selectedBook = book;
                break;
            }
        }

        if (selectedBook == null) {
            return;
        }

        requestExchange(selectedBook);
    }

    private void requestExchange(Book book) {
        int option = JOptionPane.showConfirmDialog(this,
                "\"" + book.getTitle() + "\" kitabı için takas isteği göndermek istiyor musunuz?",
                "Takas İsteği Onayı", JOptionPane.YES_NO_OPTION);

        if (option == JOptionPane.YES_OPTION) {
            // Create exchange
            com.bookexchange.model.Exchange exchange = new com.bookexchange.model.Exchange();
            exchange.setRequesterId(currentUser.getId());
            exchange.setProviderId(book.getOwnerId());
            exchange.setBookId(book.getId());

            if (dbManager.createExchange(exchange)) {
                JOptionPane.showMessageDialog(this, "Takas isteği başarıyla gönderildi",
                        "Başarılı", JOptionPane.INFORMATION_MESSAGE);
                loadBooks();
            } else {
                JOptionPane.showMessageDialog(this, "Takas isteği gönderilirken hata oluştu",
                        "Hata", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
