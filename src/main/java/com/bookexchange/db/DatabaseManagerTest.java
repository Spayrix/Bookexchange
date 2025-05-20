package com.bookexchange.db;

import com.bookexchange.config.ConfigManager;
import com.bookexchange.model.Book;
import com.bookexchange.model.Exchange;
import com.bookexchange.model.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class DatabaseManagerTest {
    private DatabaseManager dbManager;
    private User testUser;
    private Book testBook;

    @Before
    public void setUp() {
        // Set up test configuration
        ConfigManager config = ConfigManager.getInstance();
        config.setProperty(ConfigManager.DB_TYPE, "mysql"); // Use MySQL for tests
        config.setProperty(ConfigManager.DB_HOST, "localhost");
        config.setProperty(ConfigManager.DB_PORT, "3306");
        config.setProperty(ConfigManager.DB_NAME, "bookexchange_test");
        config.setProperty(ConfigManager.DB_USER, "root");
        config.setProperty(ConfigManager.DB_PASSWORD, "");
        config.saveConfig();

        // Get database manager
        dbManager = DatabaseFactory.getDatabaseManager();
        dbManager.connect();

        // Create test user
        testUser = new User("testuser", "password", "test@example.com", "Test User", "123 Test St");
        dbManager.registerUser(testUser);
        testUser = dbManager.getUserByUsername("testuser");

        // Create test book
        testBook = new Book("Test Book", "Test Author", "1234567890", "A test book", "New", testUser.getId());
        dbManager.addBook(testBook);
    }

    @After
    public void tearDown() {
        // Clean up test data
        List<Book> books = dbManager.getBooksByUser("testuser");
        for (Book book : books) {
            dbManager.deleteBook(book.getId());
        }

        // Disconnect from database
        dbManager.disconnect();
    }

    @Test
    public void testAuthentication() {
        assertTrue(dbManager.authenticateUser("testuser", "password"));
        assertFalse(dbManager.authenticateUser("testuser", "wrongpassword"));
        assertFalse(dbManager.authenticateUser("nonexistentuser", "password"));
    }

    @Test
    public void testGetUserByUsername() {
        User user = dbManager.getUserByUsername("testuser");
        assertNotNull(user);
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("Test User", user.getFullName());

        User nonExistentUser = dbManager.getUserByUsername("nonexistentuser");
        assertNull(nonExistentUser);
    }

    @Test
    public void testUpdateUser() {
        User user = dbManager.getUserByUsername("testuser");
        user.setEmail("updated@example.com");
        user.setFullName("Updated User");

        assertTrue(dbManager.updateUser(user));

        User updatedUser = dbManager.getUserByUsername("testuser");
        assertEquals("updated@example.com", updatedUser.getEmail());
        assertEquals("Updated User", updatedUser.getFullName());
    }

    @Test
    public void testAddAndGetBook() {
        Book book = new Book("Another Test Book", "Another Author", "0987654321",
                "Another test book", "Good", testUser.getId());

        assertTrue(dbManager.addBook(book));

        List<Book> books = dbManager.getBooksByUser("testuser");
        assertTrue(books.size() >= 2);

        boolean foundBook = false;
        for (Book b : books) {
            if (b.getTitle().equals("Another Test Book") && b.getAuthor().equals("Another Author")) {
                foundBook = true;
                break;
            }
        }

        assertTrue(foundBook);
    }

    @Test
    public void testUpdateBook() {
        List<Book> books = dbManager.getBooksByUser("testuser");
        Book book = null;

        for (Book b : books) {
            if (b.getTitle().equals("Test Book")) {
                book = b;
                break;
            }
        }

        assertNotNull(book);

        book.setTitle("Updated Test Book");
        book.setDescription("Updated description");

        assertTrue(dbManager.updateBook(book));

        books = dbManager.getBooksByUser("testuser");
        boolean foundUpdatedBook = false;

        for (Book b : books) {
            if (b.getTitle().equals("Updated Test Book") && b.getDescription().equals("Updated description")) {
                foundUpdatedBook = true;
                break;
            }
        }

        assertTrue(foundUpdatedBook);
    }

    @Test
    public void testDeleteBook() {
        List<Book> books = dbManager.getBooksByUser("testuser");
        Book book = null;

        for (Book b : books) {
            if (b.getTitle().equals("Test Book") || b.getTitle().equals("Updated Test Book")) {
                book = b;
                break;
            }
        }

        assertNotNull(book);

        assertTrue(dbManager.deleteBook(book.getId()));

        books = dbManager.getBooksByUser("testuser");
        boolean bookStillExists = false;

        for (Book b : books) {
            if (b.getId().equals(book.getId())) {
                bookStillExists = true;
                break;
            }
        }

        assertFalse(bookStillExists);
    }

    @Test
    public void testCreateAndUpdateExchange() {
        // Create another test user
        User anotherUser = new User("anotheruser", "password", "another@example.com", "Another User", "456 Test St");
        dbManager.registerUser(anotherUser);
        anotherUser = dbManager.getUserByUsername("anotheruser");

        // Create a book for the other user
        Book anotherBook = new Book("Another Book", "Another Author", "1122334455",
                "A book by another user", "Good", anotherUser.getId());
        dbManager.addBook(anotherBook);

        // Get the book ID
        List<Book> books = dbManager.getBooksByUser("anotheruser");
        Book book = null;

        for (Book b : books) {
            if (b.getTitle().equals("Another Book")) {
                book = b;
                break;
            }
        }

        assertNotNull(book);

        // Create exchange
        Exchange exchange = new Exchange();
        exchange.setRequesterId(testUser.getId());
        exchange.setProviderId(anotherUser.getId());
        exchange.setBookId(book.getId());

        assertTrue(dbManager.createExchange(exchange));

        // Check if book is now unavailable
        books = dbManager.getAllBooks();
        boolean bookIsUnavailable = true;

        for (Book b : books) {
            if (b.getId().equals(book.getId()) && b.isAvailable()) {
                bookIsUnavailable = false;
                break;
            }
        }

        assertTrue(bookIsUnavailable);

        // Get exchanges for test user
        List<Exchange> exchanges = dbManager.getExchangesByUser("testuser");
        Exchange createdExchange = null;

        for (Exchange e : exchanges) {
            if (e.getBookId().equals(book.getId())) {
                createdExchange = e;
                break;
            }
        }

        assertNotNull(createdExchange);
        assertEquals("PENDING", createdExchange.getStatus());

        // Update exchange status
        assertTrue(dbManager.updateExchangeStatus(createdExchange.getId(), "ACCEPTED"));

        exchanges = dbManager.getExchangesByUser("testuser");
        Exchange updatedExchange = null;

        for (Exchange e : exchanges) {
            if (e.getId().equals(createdExchange.getId())) {
                updatedExchange = e;
                break;
            }
        }

        assertNotNull(updatedExchange);
        assertEquals("ACCEPTED", updatedExchange.getStatus());

        // Cancel exchange
        assertTrue(dbManager.updateExchangeStatus(createdExchange.getId(), "CANCELLED"));

        // Check if book is available again
        books = dbManager.getAllBooks();
        boolean bookIsAvailable = false;

        for (Book b : books) {
            if (b.getId().equals(book.getId()) && b.isAvailable()) {
                bookIsAvailable = true;
                break;
            }
        }

        assertTrue(bookIsAvailable);

        // Clean up
        dbManager.deleteBook(book.getId());
    }
}