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

public class MyBooksPanel extends JPanel {
    private DatabaseManager dbManager;
    private User currentUser;
    private JTable booksTable;
    private DefaultTableModel tableModel;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton refreshButton;

    public MyBooksPanel(DatabaseManager dbManager, User currentUser) {
        this.dbManager = dbManager;
        this.currentUser = currentUser;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addButton = new JButton("Kitap Ekle");
        editButton = new JButton("Kitap Düzenle");
        deleteButton = new JButton("Kitap Sil");
        refreshButton = new JButton("Yenile");

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);

        add(buttonPanel, BorderLayout.NORTH);

        // Create table
        String[] columnNames = {"Başlık", "Yazar", "ISBN", "Durum", "Uygun"};
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
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addBook();
            }
        });

        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editBook();
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteBook();
            }
        });

        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadBooks();
            }
        });

        booksTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editBook();
                }
            }
        });

        // Load books
        loadBooks();
    }

    private void loadBooks() {
        // Clear table
        tableModel.setRowCount(0);

        // Get user's books
        List<Book> books = dbManager.getBooksByUser(currentUser.getUsername());

        // Add books to table
        for (Book book : books) {
            Object[] row = {
                    book.getTitle(),
                    book.getAuthor(),
                    book.getIsbn(),
                    book.getCondition(),
                    book.isAvailable() ? "Evet" : "Hayır"
            };
            tableModel.addRow(row);
        }
    }

    private void addBook() {
        // Create dialog
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Kitap Ekle", true);
        dialog.setSize(400, 400);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("Başlık:");
        JTextField titleField = new JTextField(20);

        JLabel authorLabel = new JLabel("Yazar:");
        JTextField authorField = new JTextField(20);

        JLabel isbnLabel = new JLabel("ISBN:");
        JTextField isbnField = new JTextField(20);

        JLabel conditionLabel = new JLabel("Durum:");
        JComboBox<String> conditionCombo = new JComboBox<>(new String[]{"Yeni", "Yeni Gibi", "Çok İyi", "İyi", "Kabul Edilebilir", "Kötü"});

        JLabel descriptionLabel = new JLabel("Açıklama:");
        JTextArea descriptionArea = new JTextArea(5, 20);
        JScrollPane descriptionScrollPane = new JScrollPane(descriptionArea);

        JButton saveButton = new JButton("Kaydet");
        JButton cancelButton = new JButton("İptal");

        panel.add(titleLabel);
        panel.add(titleField);
        panel.add(authorLabel);
        panel.add(authorField);
        panel.add(isbnLabel);
        panel.add(isbnField);
        panel.add(conditionLabel);
        panel.add(conditionCombo);
        panel.add(descriptionLabel);
        panel.add(descriptionScrollPane);
        panel.add(cancelButton);
        panel.add(saveButton);

        dialog.add(panel);

        // Add action listeners
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String title = titleField.getText();
                String author = authorField.getText();
                String isbn = isbnField.getText();
                String condition = (String) conditionCombo.getSelectedItem();
                String description = descriptionArea.getText();

                if (title.isEmpty() || author.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Başlık ve yazar zorunludur",
                            "Giriş Hatası", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Book book = new Book();
                book.setTitle(title);
                book.setAuthor(author);
                book.setIsbn(isbn);
                book.setCondition(condition);
                book.setDescription(description);
                book.setOwnerId(currentUser.getId());

                if (dbManager.addBook(book)) {
                    JOptionPane.showMessageDialog(dialog, "Kitap başarıyla eklendi",
                            "Başarılı", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadBooks();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Kitap eklenirken hata oluştu",
                            "Hata", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });

        dialog.setVisible(true);
    }

    private void editBook() {
        int selectedRow = booksTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Lütfen düzenlemek için bir kitap seçin",
                    "Seçim Gerekli", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String title = (String) tableModel.getValueAt(selectedRow, 0);
        String author = (String) tableModel.getValueAt(selectedRow, 1);

        // Find the book in the database
        List<Book> books = dbManager.getBooksByUser(currentUser.getUsername());
        Book selectedBook = null;

        for (Book book : books) {
            if (book.getTitle().equals(title) && book.getAuthor().equals(author)) {
                selectedBook = book;
                break;
            }
        }

        if (selectedBook == null) {
            return;
        }

        // Create dialog
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Kitap Düzenle", true);
        dialog.setSize(400, 400);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("Başlık:");
        JTextField titleField = new JTextField(selectedBook.getTitle(), 20);

        JLabel authorLabel = new JLabel("Yazar:");
        JTextField authorField = new JTextField(selectedBook.getAuthor(), 20);

        JLabel isbnLabel = new JLabel("ISBN:");
        JTextField isbnField = new JTextField(selectedBook.getIsbn(), 20);

        JLabel conditionLabel = new JLabel("Durum:");
        JComboBox<String> conditionCombo = new JComboBox<>(new String[]{"Yeni", "Yeni Gibi", "Çok İyi", "İyi", "Kabul Edilebilir", "Kötü"});
        conditionCombo.setSelectedItem(selectedBook.getCondition());

        JLabel descriptionLabel = new JLabel("Açıklama:");
        JTextArea descriptionArea = new JTextArea(selectedBook.getDescription(), 5, 20);
        JScrollPane descriptionScrollPane = new JScrollPane(descriptionArea);

        JButton saveButton = new JButton("Kaydet");
        JButton cancelButton = new JButton("İptal");

        panel.add(titleLabel);
        panel.add(titleField);
        panel.add(authorLabel);
        panel.add(authorField);
        panel.add(isbnLabel);
        panel.add(isbnField);
        panel.add(conditionLabel);
        panel.add(conditionCombo);
        panel.add(descriptionLabel);
        panel.add(descriptionScrollPane);
        panel.add(cancelButton);
        panel.add(saveButton);

        dialog.add(panel);

        // Make a final copy of selectedBook for use in inner classes
        final Book finalSelectedBook = selectedBook;

        // Add action listeners
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String title = titleField.getText();
                String author = authorField.getText();
                String isbn = isbnField.getText();
                String condition = (String) conditionCombo.getSelectedItem();
                String description = descriptionArea.getText();

                if (title.isEmpty() || author.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Başlık ve yazar zorunludur",
                            "Giriş Hatası", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                finalSelectedBook.setTitle(title);
                finalSelectedBook.setAuthor(author);
                finalSelectedBook.setIsbn(isbn);
                finalSelectedBook.setCondition(condition);
                finalSelectedBook.setDescription(description);

                if (dbManager.updateBook(finalSelectedBook)) {
                    JOptionPane.showMessageDialog(dialog, "Kitap başarıyla güncellendi",
                            "Başarılı", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadBooks();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Kitap güncellenirken hata oluştu",
                            "Hata", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });

        dialog.setVisible(true);
    }

    private void deleteBook() {
        int selectedRow = booksTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Lütfen silmek için bir kitap seçin",
                    "Seçim Gerekli", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String title = (String) tableModel.getValueAt(selectedRow, 0);
        String author = (String) tableModel.getValueAt(selectedRow, 1);
        boolean isAvailable = "Evet".equals(tableModel.getValueAt(selectedRow, 4));

        if (!isAvailable) {
            JOptionPane.showMessageDialog(this, "Şu anda takas işleminde olan bir kitabı silemezsiniz",
                    "Silme Hatası", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Find the book in the database
        List<Book> books = dbManager.getBooksByUser(currentUser.getUsername());
        Book selectedBook = null;

        for (Book book : books) {
            if (book.getTitle().equals(title) && book.getAuthor().equals(author)) {
                selectedBook = book;
                break;
            }
        }

        if (selectedBook == null) {
            return;
        }

        // Confirm deletion
        int option = JOptionPane.showConfirmDialog(this,
                "\"" + title + "\" kitabını silmek istediğinizden emin misiniz?",
                "Silme Onayı", JOptionPane.YES_NO_OPTION);

        if (option == JOptionPane.YES_OPTION) {
            if (dbManager.deleteBook(selectedBook.getId())) {
                JOptionPane.showMessageDialog(this, "Kitap başarıyla silindi",
                        "Başarılı", JOptionPane.INFORMATION_MESSAGE);
                loadBooks();
            } else {
                JOptionPane.showMessageDialog(this, "Kitap silinirken hata oluştu",
                        "Hata", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
