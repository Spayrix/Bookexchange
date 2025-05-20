package com.bookexchange.model;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

public class ModelTest {

    @Test
    public void testUserModel() {
        User user = new User();
        user.setId("1");
        user.setUsername("testuser");
        user.setPassword("password");
        user.setEmail("test@example.com");
        user.setFullName("Test User");
        user.setAddress("123 Test St");
        user.setExchangeCount(5);

        assertEquals("1", user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("password", user.getPassword());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("Test User", user.getFullName());
        assertEquals("123 Test St", user.getAddress());
        assertEquals(5, user.getExchangeCount());

        // Test constructor
        User user2 = new User("user2", "pass2", "user2@example.com", "User Two", "456 Test Ave");
        assertEquals("user2", user2.getUsername());
        assertEquals("pass2", user2.getPassword());
        assertEquals("user2@example.com", user2.getEmail());
        assertEquals("User Two", user2.getFullName());
        assertEquals("456 Test Ave", user2.getAddress());

        // Test toString
        assertEquals("Test User (testuser)", user.toString());
    }

    @Test
    public void testBookModel() {
        Book book = new Book();
        book.setId("1");
        book.setTitle("Test Book");
        book.setAuthor("Test Author");
        book.setIsbn("1234567890");
        book.setDescription("A test book");
        book.setCondition("New");
        book.setOwnerId("2");
        book.setOwnerName("Test Owner");
        book.setAvailable(true);
        book.setExchangeCount(3);

        assertEquals("1", book.getId());
        assertEquals("Test Book", book.getTitle());
        assertEquals("Test Author", book.getAuthor());
        assertEquals("1234567890", book.getIsbn());
        assertEquals("A test book", book.getDescription());
        assertEquals("New", book.getCondition());
        assertEquals("2", book.getOwnerId());
        assertEquals("Test Owner", book.getOwnerName());
        assertTrue(book.isAvailable());
        assertEquals(3, book.getExchangeCount());

        // Test constructor
        Book book2 = new Book("Book Two", "Author Two", "0987654321", "Another book", "Good", "3");
        assertEquals("Book Two", book2.getTitle());
        assertEquals("Author Two", book2.getAuthor());
        assertEquals("0987654321", book2.getIsbn());
        assertEquals("Another book", book2.getDescription());
        assertEquals("Good", book2.getCondition());
        assertEquals("3", book2.getOwnerId());
        assertTrue(book2.isAvailable());

        // Test toString
        assertEquals("Test Book by Test Author", book.toString());
    }

    @Test
    public void testExchangeModel() {
        Exchange exchange = new Exchange();
        exchange.setId("1");
        exchange.setRequesterId("2");
        exchange.setRequesterName("Requester");
        exchange.setProviderId("3");
        exchange.setProviderName("Provider");
        exchange.setBookId("4");
        exchange.setBookTitle("Book Title");
        exchange.setStatus("PENDING");

        Date requestDate = new Date();
        exchange.setRequestDate(requestDate);

        Date completionDate = new Date();
        exchange.setCompletionDate(completionDate);

        assertEquals("1", exchange.getId());
        assertEquals("2", exchange.getRequesterId());
        assertEquals("Requester", exchange.getRequesterName());
        assertEquals("3", exchange.getProviderId());
        assertEquals("Provider", exchange.getProviderName());
        assertEquals("4", exchange.getBookId());
        assertEquals("Book Title", exchange.getBookTitle());
        assertEquals("PENDING", exchange.getStatus());
        assertEquals(requestDate, exchange.getRequestDate());
        assertEquals(completionDate, exchange.getCompletionDate());

        // Test default constructor
        Exchange exchange2 = new Exchange();
        assertEquals("PENDING", exchange2.getStatus());
        assertNotNull(exchange2.getRequestDate());

        // Test toString
        assertEquals("Book Title - PENDING", exchange.toString());
    }
}