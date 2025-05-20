package com.bookexchange.db;

import com.bookexchange.model.Book;
import com.bookexchange.model.Exchange;
import com.bookexchange.model.User;

import java.util.List;

public interface DatabaseManager {
    // Connection methods
    void connect();
    void disconnect();
    boolean isConnected();

    // User methods
    boolean authenticateUser(String username, String password);
    User getUserByUsername(String username);
    boolean registerUser(User user);
    boolean updateUser(User user);

    // Book methods
    List<Book> getAllBooks();
    List<Book> getBooksByUser(String username);
    boolean addBook(Book book);
    boolean updateBook(Book book);
    boolean deleteBook(String bookId);

    // Exchange methods
    List<Exchange> getExchangesByUser(String username);
    boolean createExchange(Exchange exchange);
    boolean updateExchangeStatus(String exchangeId, String status);

    // Reporting methods
    List<Book> getMostExchangedBooks(int limit);
    List<User> getMostActiveUsers(int limit);
    int getTotalExchanges();
    int getTotalBooks();
    int getTotalUsers();
}