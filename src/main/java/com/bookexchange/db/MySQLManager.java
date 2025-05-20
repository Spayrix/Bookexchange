package com.bookexchange.db;

import com.bookexchange.config.ConfigManager;
import com.bookexchange.model.Book;
import com.bookexchange.model.Exchange;
import com.bookexchange.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySQLManager implements DatabaseManager {
    private Connection connection;

    @Override
    public void connect() {
        try {
            ConfigManager config = ConfigManager.getInstance();
            String host = config.getProperty(ConfigManager.DB_HOST);
            String port = config.getProperty(ConfigManager.DB_PORT);
            String dbName = config.getProperty(ConfigManager.DB_NAME);
            String user = config.getProperty(ConfigManager.DB_USER);
            String password = config.getProperty(ConfigManager.DB_PASSWORD);

            // First connect without database to check if it exists
            String baseUrl = "jdbc:mysql://" + host + ":" + port;

            // Load the MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Establish connection to server (without specifying database)
            System.out.println("Connecting to MySQL server at " + baseUrl);
            connection = DriverManager.getConnection(baseUrl, user, password);

            // Check if database exists, create if it doesn't
            createDatabaseIfNotExists(dbName);

            // Close the initial connection
            connection.close();

            // Connect with the database specified
            String dbUrl = baseUrl + "/" + dbName + "?useSSL=false&allowPublicKeyRetrieval=true";
            System.out.println("Connecting to database: " + dbUrl);
            connection = DriverManager.getConnection(dbUrl, user, password);

            // Create tables if they don't exist
            if (connection != null) {
                System.out.println("Connected to database: " + dbName);
                createTablesIfNotExist();
            }
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Error connecting to MySQL database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createDatabaseIfNotExists(String dbName) {
        try {
            Statement stmt = connection.createStatement();
            System.out.println("Creating database if not exists: " + dbName);
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + dbName);
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Error creating database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createTablesIfNotExist() {
        try {
            Statement stmt = connection.createStatement();

            // Users table
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "username VARCHAR(50) UNIQUE NOT NULL," +
                    "password VARCHAR(100) NOT NULL," +
                    "email VARCHAR(100) UNIQUE NOT NULL," +
                    "full_name VARCHAR(100)," +
                    "address TEXT," +
                    "registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            // Books table - Note the backticks around `condition` as it's a reserved word
            stmt.execute("CREATE TABLE IF NOT EXISTS books (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "title VARCHAR(100) NOT NULL," +
                    "author VARCHAR(100) NOT NULL," +
                    "isbn VARCHAR(20)," +
                    "description TEXT," +
                    "`condition` VARCHAR(20)," +
                    "owner_id INT NOT NULL," +
                    "available BOOLEAN DEFAULT TRUE," +
                    "added_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (owner_id) REFERENCES users(id)" +
                    ")");

            // Exchanges table
            stmt.execute("CREATE TABLE IF NOT EXISTS exchanges (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "requester_id INT NOT NULL," +
                    "provider_id INT NOT NULL," +
                    "book_id INT NOT NULL," +
                    "status VARCHAR(20) DEFAULT 'PENDING'," +
                    "request_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "completion_date TIMESTAMP NULL," +
                    "FOREIGN KEY (requester_id) REFERENCES users(id)," +
                    "FOREIGN KEY (provider_id) REFERENCES users(id)," +
                    "FOREIGN KEY (book_id) REFERENCES books(id)" +
                    ")");

            stmt.close();
        } catch (SQLException e) {
            System.err.println("Error creating tables: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                connection = null;
            }
        } catch (SQLException e) {
            System.err.println("Error disconnecting from database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public boolean authenticateUser(String username, String password) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT * FROM users WHERE username = ? AND password = ?");
            stmt.setString(1, username);
            stmt.setString(2, password); // In a real app, use password hashing

            ResultSet rs = stmt.executeQuery();
            boolean authenticated = rs.next();

            rs.close();
            stmt.close();

            return authenticated;
        } catch (SQLException e) {
            System.err.println("Error authenticating user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public User getUserByUsername(String username) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT * FROM users WHERE username = ?");
            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();
            User user = null;

            if (rs.next()) {
                user = new User();
                user.setId(String.valueOf(rs.getInt("id")));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setFullName(rs.getString("full_name"));
                user.setAddress(rs.getString("address"));
            }

            rs.close();
            stmt.close();

            return user;
        } catch (SQLException e) {
            System.err.println("Error getting user by username: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean registerUser(User user) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO users (username, password, email, full_name, address) VALUES (?, ?, ?, ?, ?)");
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword()); // In a real app, use password hashing
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getFullName());
            stmt.setString(5, user.getAddress());

            int rowsAffected = stmt.executeUpdate();
            stmt.close();

            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error registering user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateUser(User user) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "UPDATE users SET email = ?, full_name = ?, address = ? WHERE id = ?");
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getFullName());
            stmt.setString(3, user.getAddress());
            stmt.setInt(4, Integer.parseInt(user.getId()));

            int rowsAffected = stmt.executeUpdate();
            stmt.close();

            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();

        if (!isConnected()) {
            System.err.println("Database connection is not established");
            return books;
        }

        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(
                    "SELECT b.*, u.username as owner_name FROM books b " +
                            "JOIN users u ON b.owner_id = u.id " +
                            "WHERE b.available = TRUE");

            while (rs.next()) {
                Book book = new Book();
                book.setId(String.valueOf(rs.getInt("id")));
                book.setTitle(rs.getString("title"));
                book.setAuthor(rs.getString("author"));
                book.setIsbn(rs.getString("isbn"));
                book.setDescription(rs.getString("description"));
                book.setCondition(rs.getString("condition"));
                book.setOwnerId(String.valueOf(rs.getInt("owner_id")));
                book.setOwnerName(rs.getString("owner_name"));
                book.setAvailable(rs.getBoolean("available"));
                books.add(book);

                // Debug output
                System.out.println("Found available book: " + book.getTitle() + " by " + book.getAuthor());
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Error getting all books: " + e.getMessage());
            e.printStackTrace();
        }

        return books;
    }

    @Override
    public List<Book> getBooksByUser(String username) {
        List<Book> books = new ArrayList<>();

        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT b.* FROM books b " +
                            "JOIN users u ON b.owner_id = u.id " +
                            "WHERE u.username = ?");
            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Book book = new Book();
                book.setId(String.valueOf(rs.getInt("id")));
                book.setTitle(rs.getString("title"));
                book.setAuthor(rs.getString("author"));
                book.setIsbn(rs.getString("isbn"));
                book.setDescription(rs.getString("description"));
                book.setCondition(rs.getString("condition"));
                book.setOwnerId(String.valueOf(rs.getInt("owner_id")));
                book.setAvailable(rs.getBoolean("available"));
                books.add(book);
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Error getting books by user: " + e.getMessage());
            e.printStackTrace();
        }

        return books;
    }

    @Override
    public boolean addBook(Book book) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO books (title, author, isbn, description, `condition`, owner_id) " +
                            "VALUES (?, ?, ?, ?, ?, ?)");
            stmt.setString(1, book.getTitle());
            stmt.setString(2, book.getAuthor());
            stmt.setString(3, book.getIsbn());
            stmt.setString(4, book.getDescription());
            stmt.setString(5, book.getCondition());
            stmt.setInt(6, Integer.parseInt(book.getOwnerId()));

            int rowsAffected = stmt.executeUpdate();
            stmt.close();

            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error adding book: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateBook(Book book) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "UPDATE books SET title = ?, author = ?, isbn = ?, " +
                            "description = ?, `condition` = ?, available = ? WHERE id = ?");
            stmt.setString(1, book.getTitle());
            stmt.setString(2, book.getAuthor());
            stmt.setString(3, book.getIsbn());
            stmt.setString(4, book.getDescription());
            stmt.setString(5, book.getCondition());
            stmt.setBoolean(6, book.isAvailable());
            stmt.setInt(7, Integer.parseInt(book.getId()));

            int rowsAffected = stmt.executeUpdate();
            stmt.close();

            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating book: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteBook(String bookId) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "DELETE FROM books WHERE id = ?");
            stmt.setInt(1, Integer.parseInt(bookId));

            int rowsAffected = stmt.executeUpdate();
            stmt.close();

            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting book: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Exchange> getExchangesByUser(String username) {
        List<Exchange> exchanges = new ArrayList<>();

        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT e.*, b.title, requester.username as requester_name, provider.username as provider_name " +
                            "FROM exchanges e " +
                            "JOIN books b ON e.book_id = b.id " +
                            "JOIN users requester ON e.requester_id = requester.id " +
                            "JOIN users provider ON e.provider_id = provider.id " +
                            "WHERE requester.username = ? OR provider.username = ?");
            stmt.setString(1, username);
            stmt.setString(2, username);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Exchange exchange = new Exchange();
                exchange.setId(String.valueOf(rs.getInt("id")));
                exchange.setRequesterId(String.valueOf(rs.getInt("requester_id")));
                exchange.setProviderId(String.valueOf(rs.getInt("provider_id")));
                exchange.setBookId(String.valueOf(rs.getInt("book_id")));
                exchange.setBookTitle(rs.getString("title"));
                exchange.setRequesterName(rs.getString("requester_name"));
                exchange.setProviderName(rs.getString("provider_name"));
                exchange.setStatus(rs.getString("status"));
                exchange.setRequestDate(rs.getTimestamp("request_date"));
                exchange.setCompletionDate(rs.getTimestamp("completion_date"));
                exchanges.add(exchange);
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Error getting exchanges by user: " + e.getMessage());
            e.printStackTrace();
        }

        return exchanges;
    }

    @Override
    public boolean createExchange(Exchange exchange) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO exchanges (requester_id, provider_id, book_id) " +
                            "VALUES (?, ?, ?)");
            stmt.setInt(1, Integer.parseInt(exchange.getRequesterId()));
            stmt.setInt(2, Integer.parseInt(exchange.getProviderId()));
            stmt.setInt(3, Integer.parseInt(exchange.getBookId()));

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                // Mark book as unavailable
                PreparedStatement updateBook = connection.prepareStatement(
                        "UPDATE books SET available = FALSE WHERE id = ?");
                updateBook.setInt(1, Integer.parseInt(exchange.getBookId()));
                updateBook.executeUpdate();
                updateBook.close();
            }

            stmt.close();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error creating exchange: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateExchangeStatus(String exchangeId, String status) {
        try {
            PreparedStatement stmt;

            if ("COMPLETED".equals(status)) {
                stmt = connection.prepareStatement(
                        "UPDATE exchanges SET status = ?, completion_date = CURRENT_TIMESTAMP WHERE id = ?");
                stmt.setString(1, status);
                stmt.setInt(2, Integer.parseInt(exchangeId));
            } else if ("CANCELLED".equals(status)) {
                stmt = connection.prepareStatement(
                        "UPDATE exchanges SET status = ? WHERE id = ?");
                stmt.setString(1, status);
                stmt.setInt(2, Integer.parseInt(exchangeId));

                // Make the book available again
                PreparedStatement getBookId = connection.prepareStatement(
                        "SELECT book_id FROM exchanges WHERE id = ?");
                getBookId.setInt(1, Integer.parseInt(exchangeId));
                ResultSet rs = getBookId.executeQuery();

                if (rs.next()) {
                    int bookId = rs.getInt("book_id");
                    PreparedStatement updateBook = connection.prepareStatement(
                            "UPDATE books SET available = TRUE WHERE id = ?");
                    updateBook.setInt(1, bookId);
                    updateBook.executeUpdate();
                    updateBook.close();
                }

                rs.close();
                getBookId.close();
            } else {
                stmt = connection.prepareStatement(
                        "UPDATE exchanges SET status = ? WHERE id = ?");
                stmt.setString(1, status);
                stmt.setInt(2, Integer.parseInt(exchangeId));
            }

            int rowsAffected = stmt.executeUpdate();
            stmt.close();

            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating exchange status: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Book> getMostExchangedBooks(int limit) {
        List<Book> books = new ArrayList<>();

        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT b.*, COUNT(e.id) as exchange_count, u.username as owner_name " +
                            "FROM books b " +
                            "JOIN exchanges e ON b.id = e.book_id " +
                            "JOIN users u ON b.owner_id = u.id " +
                            "GROUP BY b.id " +
                            "ORDER BY exchange_count DESC " +
                            "LIMIT ?");
            stmt.setInt(1, limit);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Book book = new Book();
                book.setId(String.valueOf(rs.getInt("id")));
                book.setTitle(rs.getString("title"));
                book.setAuthor(rs.getString("author"));
                book.setIsbn(rs.getString("isbn"));
                book.setOwnerId(String.valueOf(rs.getInt("owner_id")));
                book.setOwnerName(rs.getString("owner_name"));
                book.setExchangeCount(rs.getInt("exchange_count"));
                books.add(book);
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Error getting most exchanged books: " + e.getMessage());
            e.printStackTrace();
        }

        return books;
    }

    @Override
    public List<User> getMostActiveUsers(int limit) {
        List<User> users = new ArrayList<>();

        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT u.*, " +
                            "(SELECT COUNT(*) FROM exchanges e WHERE e.requester_id = u.id OR e.provider_id = u.id) as exchange_count " +
                            "FROM users u " +
                            "ORDER BY exchange_count DESC " +
                            "LIMIT ?");
            stmt.setInt(1, limit);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                User user = new User();
                user.setId(String.valueOf(rs.getInt("id")));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setFullName(rs.getString("full_name"));
                user.setExchangeCount(rs.getInt("exchange_count"));
                users.add(user);
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Error getting most active users: " + e.getMessage());
            e.printStackTrace();
        }

        return users;
    }

    @Override
    public int getTotalExchanges() {
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as total FROM exchanges");

            int total = 0;
            if (rs.next()) {
                total = rs.getInt("total");
            }

            rs.close();
            stmt.close();

            return total;
        } catch (SQLException e) {
            System.err.println("Error getting total exchanges: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int getTotalBooks() {
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as total FROM books");

            int total = 0;
            if (rs.next()) {
                total = rs.getInt("total");
            }

            rs.close();
            stmt.close();

            return total;
        } catch (SQLException e) {
            System.err.println("Error getting total books: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int getTotalUsers() {
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as total FROM users");

            int total = 0;
            if (rs.next()) {
                total = rs.getInt("total");
            }

            rs.close();
            stmt.close();

            return total;
        } catch (SQLException e) {
            System.err.println("Error getting total users: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
}
