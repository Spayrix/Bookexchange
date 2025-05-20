package com.bookexchange.model;

public class Book {
    private String id;
    private String title;
    private String author;
    private String isbn;
    private String description;
    private String condition;
    private String ownerId;
    private String ownerName;
    private boolean available;
    private int exchangeCount;

    public Book() {
        this.available = true;
    }

    public Book(String title, String author, String isbn, String description, String condition, String ownerId) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.description = description;
        this.condition = condition;
        this.ownerId = ownerId;
        this.available = true;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setId(int id) {
        this.id = String.valueOf(id);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = String.valueOf(ownerId);
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public int getExchangeCount() {
        return exchangeCount;
    }

    public void setExchangeCount(int exchangeCount) {
        this.exchangeCount = exchangeCount;
    }

    @Override
    public String toString() {
        return title + " by " + author;
    }
}