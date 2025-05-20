package com.bookexchange.ui;

import com.bookexchange.config.ConfigManager;
import com.bookexchange.db.DatabaseFactory;
import com.bookexchange.db.DatabaseManager;
import com.bookexchange.model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainFrame extends JFrame {
    private User currentUser;
    private DatabaseManager dbManager;
    private JTabbedPane tabbedPane;

    // Tabs
    private BooksPanel booksPanel;
    private MyBooksPanel myBooksPanel;
    private ExchangesPanel exchangesPanel;
    private ReportsPanel reportsPanel;
    private SettingsPanel settingsPanel;

    public MainFrame(User user) {
        this.currentUser = user;
        this.dbManager = DatabaseFactory.getDatabaseManager();

        // Ensure database connection
        if (!dbManager.isConnected()) {
            try {
                dbManager.connect();
                if (!dbManager.isConnected()) {
                    JOptionPane.showMessageDialog(this,
                            "Veritabanına bağlanılamıyor. Lütfen ayarlarınızı kontrol edin.",
                            "Veritabanı Hatası",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Veritabanına bağlanırken hata: " + e.getMessage(),
                        "Veritabanı Hatası",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }

        setTitle("Kitap Takası Sistemi - " + user.getUsername());
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Add window listener to close database connection
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (dbManager != null) {
                    dbManager.disconnect();
                }
            }
        });

        // Create tabbed pane
        tabbedPane = new JTabbedPane();

        // Create panels for each tab
        try {
            booksPanel = new BooksPanel(dbManager, currentUser);
            myBooksPanel = new MyBooksPanel(dbManager, currentUser);
            exchangesPanel = new ExchangesPanel(dbManager, currentUser);
            reportsPanel = new ReportsPanel(dbManager);
            settingsPanel = new SettingsPanel();

            // Add tabs
            tabbedPane.addTab("Mevcut Kitaplar", new ImageIcon(), booksPanel, "Mevcut kitaplara göz at");
            tabbedPane.addTab("Kitaplarım", new ImageIcon(), myBooksPanel, "Kitaplarınızı yönetin");
            tabbedPane.addTab("Takaslar", new ImageIcon(), exchangesPanel, "Takasları görüntüleyin ve yönetin");
            tabbedPane.addTab("Raporlar", new ImageIcon(), reportsPanel, "Sistem raporlarını görüntüleyin");
            tabbedPane.addTab("Ayarlar", new ImageIcon(), settingsPanel, "Uygulama ayarlarını yapılandırın");

            // Add tabbed pane to frame
            add(tabbedPane);

            // Create menu bar
            createMenuBar();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Uygulama başlatılırken hata: " + e.getMessage(),
                    "Uygulama Hatası",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // File menu
        JMenu fileMenu = new JMenu("Dosya");
        JMenuItem exitItem = new JMenuItem("Çıkış");
        exitItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (dbManager != null) {
                    dbManager.disconnect();
                }
                System.exit(0);
            }
        });
        fileMenu.add(exitItem);

        // User menu
        JMenu userMenu = new JMenu("Kullanıcı");
        JMenuItem profileItem = new JMenuItem("Profil");
        profileItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openProfileDialog();
            }
        });
        JMenuItem logoutItem = new JMenuItem("Çıkış Yap");
        logoutItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logout();
            }
        });
        userMenu.add(profileItem);
        userMenu.add(logoutItem);

        // Help menu
        JMenu helpMenu = new JMenu("Yardım");
        JMenuItem aboutItem = new JMenuItem("Hakkında");
        aboutItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAboutDialog();
            }
        });
        helpMenu.add(aboutItem);

        // Add menus to menu bar
        menuBar.add(fileMenu);
        menuBar.add(userMenu);
        menuBar.add(helpMenu);

        // Set menu bar
        setJMenuBar(menuBar);
    }

    private void openProfileDialog() {
        JDialog dialog = new JDialog(this, "Kullanıcı Profili", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Form panel
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 5, 5));

        JLabel emailLabel = new JLabel("E-posta:");
        JTextField emailField = new JTextField(currentUser.getEmail(), 20);

        JLabel fullNameLabel = new JLabel("Ad Soyad:");
        JTextField fullNameField = new JTextField(currentUser.getFullName(), 20);

        JLabel addressLabel = new JLabel("Adres:");
        JTextArea addressArea = new JTextArea(currentUser.getAddress(), 3, 20);
        JScrollPane addressScrollPane = new JScrollPane(addressArea);

        formPanel.add(emailLabel);
        formPanel.add(emailField);
        formPanel.add(fullNameLabel);
        formPanel.add(fullNameField);
        formPanel.add(addressLabel);
        formPanel.add(addressScrollPane);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton saveButton = new JButton("Kaydet");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentUser.setEmail(emailField.getText());
                currentUser.setFullName(fullNameField.getText());
                currentUser.setAddress(addressArea.getText());

                if (dbManager.updateUser(currentUser)) {
                    JOptionPane.showMessageDialog(dialog, "Profil başarıyla güncellendi",
                            "Başarılı", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Profil güncellenirken hata oluştu",
                            "Hata", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JButton cancelButton = new JButton("İptal");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void logout() {
        if (dbManager != null) {
            dbManager.disconnect();
        }

        LoginFrame loginFrame = new LoginFrame();
        loginFrame.setVisible(true);
        dispose();
    }

    private void showAboutDialog() {
        JOptionPane.showMessageDialog(this,
                "Kitap Takası Sistemi\nSürüm 1.0\n\nKitap takası için basit bir uygulama.\n\n© 2023 Tüm hakları saklıdır.",
                "Hakkında", JOptionPane.INFORMATION_MESSAGE);
    }
}
