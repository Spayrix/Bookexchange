package com.bookexchange.db;

import com.bookexchange.config.ConfigManager;
import com.bookexchange.model.Book;
import com.bookexchange.model.Exchange;
import com.bookexchange.model.User;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import com.mongodb.client.model.Accumulators;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MongoDBManager implements DatabaseManager {
    private MongoClient mongoClient;
    private MongoDatabase database;

    @Override
    public void connect() {
        ConfigManager config = ConfigManager.getInstance();
        String host = config.getProperty(ConfigManager.DB_HOST);
        String port = config.getProperty(ConfigManager.DB_PORT);
        String dbName = config.getProperty(ConfigManager.DB_NAME);
        String user = config.getProperty(ConfigManager.DB_USER);
        String password = config.getProperty(ConfigManager.DB_PASSWORD);

        String uri;
        if (user != null && !user.isEmpty() && password != null && !password.isEmpty()) {
            uri = "mongodb://" + user + ":" + password + "@" + host + ":" + port;
        } else {
            uri = "mongodb://" + host + ":" + port;
        }

        mongoClient = MongoClients.create(uri);
        database = mongoClient.getDatabase(dbName);
    }

    @Override
    public void disconnect() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }

    @Override
    public boolean isConnected() {
        return mongoClient != null;
    }

    @Override
    public boolean authenticateUser(String username, String password) {
        MongoCollection<Document> users = database.getCollection("users");
        Document query = new Document("username", username)
                .append("password", password); // In a real app, use password hashing

        return users.find(query).first() != null;
    }

    @Override
    public User getUserByUsername(String username) {
        MongoCollection<Document> users = database.getCollection("users");
        Document query = new Document("username", username);
        Document userDoc = users.find(query).first();

        if (userDoc != null) {
            User user = new User();
            user.setId(userDoc.getObjectId("_id").toString());
            user.setUsername(userDoc.getString("username"));
            user.setEmail(userDoc.getString("email"));
            user.setFullName(userDoc.getString("fullName"));
            user.setAddress(userDoc.getString("address"));
            return user;
        }

        return null;
    }

    @Override
    public boolean registerUser(User user) {
        MongoCollection<Document> users = database.getCollection("users");

        Document userDoc = new Document("username", user.getUsername())
                .append("password", user.getPassword()) // In a real app, use password hashing
                .append("email", user.getEmail())
                .append("fullName", user.getFullName())
                .append("address", user.getAddress())
                .append("registrationDate", new Date());

        try {
            users.insertOne(userDoc);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateUser(User user) {
        MongoCollection<Document> users = database.getCollection("users");

        Document query = new Document("_id", new ObjectId(user.getId()));
        Document update = new Document("$set", new Document("email", user.getEmail())
                .append("fullName", user.getFullName())
                .append("address", user.getAddress()));

        try {
            users.updateOne(query, update);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        MongoCollection<Document> booksCollection = database.getCollection("books");
        MongoCollection<Document> usersCollection = database.getCollection("users");

        FindIterable<Document> bookDocs = booksCollection.find(Filters.eq("available", true));

        for (Document bookDoc : bookDocs) {
            Book book = new Book();
            book.setId(bookDoc.getObjectId("_id").toString());
            book.setTitle(bookDoc.getString("title"));
            book.setAuthor(bookDoc.getString("author"));
            book.setIsbn(bookDoc.getString("isbn"));
            book.setDescription(bookDoc.getString("description"));
            book.setCondition(bookDoc.getString("condition"));
            book.setOwnerId(bookDoc.getObjectId("ownerId").toString());
            book.setAvailable(bookDoc.getBoolean("available", true));

            // Get owner name
            Document ownerDoc = usersCollection.find(Filters.eq("_id", new ObjectId(book.getOwnerId()))).first();
            if (ownerDoc != null) {
                book.setOwnerName(ownerDoc.getString("username"));
            }

            books.add(book);
        }

        return books;
    }

    @Override
    public List<Book> getBooksByUser(String username) {
        List<Book> books = new ArrayList<>();
        MongoCollection<Document> booksCollection = database.getCollection("books");
        MongoCollection<Document> usersCollection = database.getCollection("users");

        Document userDoc = usersCollection.find(Filters.eq("username", username)).first();
        if (userDoc == null) {
            return books;
        }

        ObjectId userId = userDoc.getObjectId("_id");
        FindIterable<Document> bookDocs = booksCollection.find(Filters.eq("ownerId", userId));

        for (Document bookDoc : bookDocs) {
            Book book = new Book();
            book.setId(bookDoc.getObjectId("_id").toString());
            book.setTitle(bookDoc.getString("title"));
            book.setAuthor(bookDoc.getString("author"));
            book.setIsbn(bookDoc.getString("isbn"));
            book.setDescription(bookDoc.getString("description"));
            book.setCondition(bookDoc.getString("condition"));
            book.setOwnerId(bookDoc.getObjectId("ownerId").toString());
            book.setAvailable(bookDoc.getBoolean("available", true));
            books.add(book);
        }

        return books;
    }

    @Override
    public boolean addBook(Book book) {
        MongoCollection<Document> books = database.getCollection("books");

        Document bookDoc = new Document("title", book.getTitle())
                .append("author", book.getAuthor())
                .append("isbn", book.getIsbn())
                .append("description", book.getDescription())
                .append("condition", book.getCondition())
                .append("ownerId", new ObjectId(book.getOwnerId()))
                .append("available", true)
                .append("addedDate", new Date());

        try {
            books.insertOne(bookDoc);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateBook(Book book) {
        MongoCollection<Document> books = database.getCollection("books");

        Document query = new Document("_id", new ObjectId(book.getId()));
        Document update = new Document("$set", new Document("title", book.getTitle())
                .append("author", book.getAuthor())
                .append("isbn", book.getIsbn())
                .append("description", book.getDescription())
                .append("condition", book.getCondition())
                .append("available", book.isAvailable()));

        try {
            books.updateOne(query, update);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteBook(String bookId) {
        MongoCollection<Document> books = database.getCollection("books");

        Document query = new Document("_id", new ObjectId(bookId));

        try {
            books.deleteOne(query);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Exchange> getExchangesByUser(String username) {
        List<Exchange> exchanges = new ArrayList<>();
        MongoCollection<Document> exchangesCollection = database.getCollection("exchanges");
        MongoCollection<Document> booksCollection = database.getCollection("books");
        MongoCollection<Document> usersCollection = database.getCollection("users");

        Document userDoc = usersCollection.find(Filters.eq("username", username)).first();
        if (userDoc == null) {
            return exchanges;
        }

        ObjectId userId = userDoc.getObjectId("_id");
        FindIterable<Document> exchangeDocs = exchangesCollection.find(
                Filters.or(
                        Filters.eq("requesterId", userId),
                        Filters.eq("providerId", userId)
                )
        );

        for (Document exchangeDoc : exchangeDocs) {
            Exchange exchange = new Exchange();
            exchange.setId(exchangeDoc.getObjectId("_id").toString());
            exchange.setRequesterId(exchangeDoc.getObjectId("requesterId").toString());
            exchange.setProviderId(exchangeDoc.getObjectId("providerId").toString());
            exchange.setBookId(exchangeDoc.getObjectId("bookId").toString());
            exchange.setStatus(exchangeDoc.getString("status"));
            exchange.setRequestDate(exchangeDoc.getDate("requestDate"));
            exchange.setCompletionDate(exchangeDoc.getDate("completionDate"));

            // Get book title
            Document bookDoc = booksCollection.find(Filters.eq("_id", new ObjectId(exchange.getBookId()))).first();
            if (bookDoc != null) {
                exchange.setBookTitle(bookDoc.getString("title"));
            }

            // Get requester name
            Document requesterDoc = usersCollection.find(Filters.eq("_id", new ObjectId(exchange.getRequesterId()))).first();
            if (requesterDoc != null) {
                exchange.setRequesterName(requesterDoc.getString("username"));
            }

            // Get provider name
            Document providerDoc = usersCollection.find(Filters.eq("_id", new ObjectId(exchange.getProviderId()))).first();
            if (providerDoc != null) {
                exchange.setProviderName(providerDoc.getString("username"));
            }

            exchanges.add(exchange);
        }

        return exchanges;
    }

    @Override
    public boolean createExchange(Exchange exchange) {
        MongoCollection<Document> exchanges = database.getCollection("exchanges");
        MongoCollection<Document> books = database.getCollection("books");

        Document exchangeDoc = new Document("requesterId", new ObjectId(exchange.getRequesterId()))
                .append("providerId", new ObjectId(exchange.getProviderId()))
                .append("bookId", new ObjectId(exchange.getBookId()))
                .append("status", "PENDING")
                .append("requestDate", new Date());

        try {
            exchanges.insertOne(exchangeDoc);

            // Mark book as unavailable
            books.updateOne(
                    Filters.eq("_id", new ObjectId(exchange.getBookId())),
                    Updates.set("available", false)
            );

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateExchangeStatus(String exchangeId, String status) {
        MongoCollection<Document> exchanges = database.getCollection("exchanges");
        MongoCollection<Document> books = database.getCollection("books");

        Document query = new Document("_id", new ObjectId(exchangeId));
        Document update;

        if ("COMPLETED".equals(status)) {
            update = new Document("$set", new Document("status", status)
                    .append("completionDate", new Date()));
        } else {
            update = new Document("$set", new Document("status", status));
        }

        try {
            exchanges.updateOne(query, update);

            if ("CANCELLED".equals(status)) {
                // Make the book available again
                Document exchangeDoc = exchanges.find(query).first();
                if (exchangeDoc != null) {
                    ObjectId bookId = exchangeDoc.getObjectId("bookId");
                    books.updateOne(
                            Filters.eq("_id", bookId),
                            Updates.set("available", true)
                    );
                }
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Book> getMostExchangedBooks(int limit) {
        List<Book> books = new ArrayList<>();
        MongoCollection<Document> booksCollection = database.getCollection("books");
        MongoCollection<Document> exchangesCollection = database.getCollection("exchanges");
        MongoCollection<Document> usersCollection = database.getCollection("users");

        // Using a simpler approach for aggregation
        List<Document> results = new ArrayList<>();

        try {
            // First, count exchanges per book
            Document groupStage = new Document("$group",
                    new Document("_id", "$bookId")
                            .append("count", new Document("$sum", 1))
                            .append("bookId", new Document("$first", "$bookId"))
            );

            Document sortStage = new Document("$sort",
                    new Document("count", -1)
            );

            Document limitStage = new Document("$limit", limit);

            List<Document> pipeline = Arrays.asList(groupStage, sortStage, limitStage);

            exchangesCollection.aggregate(pipeline).into(results);

            // Process results
            for (Document result : results) {
                ObjectId bookId = result.get("bookId", ObjectId.class);
                int exchangeCount = result.getInteger("count");

                Document bookDoc = booksCollection.find(Filters.eq("_id", bookId)).first();
                if (bookDoc != null) {
                    Book book = new Book();
                    book.setId(bookDoc.getObjectId("_id").toString());
                    book.setTitle(bookDoc.getString("title"));
                    book.setAuthor(bookDoc.getString("author"));
                    book.setIsbn(bookDoc.getString("isbn"));
                    book.setOwnerId(bookDoc.getObjectId("ownerId").toString());
                    book.setExchangeCount(exchangeCount);

                    // Get owner name
                    Document ownerDoc = usersCollection.find(Filters.eq("_id", new ObjectId(book.getOwnerId()))).first();
                    if (ownerDoc != null) {
                        book.setOwnerName(ownerDoc.getString("username"));
                    }

                    books.add(book);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return books;
    }

    @Override
    public List<User> getMostActiveUsers(int limit) {
        List<User> users = new ArrayList<>();
        MongoCollection<Document> usersCollection = database.getCollection("users");
        MongoCollection<Document> exchangesCollection = database.getCollection("exchanges");

        try {
            // Create a map to store user exchange counts
            java.util.Map<String, Integer> userExchanges = new java.util.HashMap<>();

            // Count requester exchanges
            Document requesterGroup = new Document("$group",
                    new Document("_id", "$requesterId")
                            .append("count", new Document("$sum", 1))
            );

            List<Document> requesterPipeline = Arrays.asList(requesterGroup);
            List<Document> requesterResults = new ArrayList<>();
            exchangesCollection.aggregate(requesterPipeline).into(requesterResults);

            for (Document result : requesterResults) {
                ObjectId userId = result.getObjectId("_id");
                int count = result.getInteger("count");
                userExchanges.put(userId.toString(), count);
            }

            // Count provider exchanges
            Document providerGroup = new Document("$group",
                    new Document("_id", "$providerId")
                            .append("count", new Document("$sum", 1))
            );

            List<Document> providerPipeline = Arrays.asList(providerGroup);
            List<Document> providerResults = new ArrayList<>();
            exchangesCollection.aggregate(providerPipeline).into(providerResults);

            for (Document result : providerResults) {
                ObjectId userId = result.getObjectId("_id");
                int count = result.getInteger("count");
                String userIdStr = userId.toString();

                if (userExchanges.containsKey(userIdStr)) {
                    userExchanges.put(userIdStr, userExchanges.get(userIdStr) + count);
                } else {
                    userExchanges.put(userIdStr, count);
                }
            }

            // Sort users by exchange count
            List<java.util.Map.Entry<String, Integer>> sortedUsers = new ArrayList<>(userExchanges.entrySet());
            sortedUsers.sort((a, b) -> b.getValue() - a.getValue());

            // Get top users
            int count = 0;
            for (java.util.Map.Entry<String, Integer> entry : sortedUsers) {
                if (count >= limit) break;

                String userIdStr = entry.getKey();
                Document userDoc = usersCollection.find(Filters.eq("_id", new ObjectId(userIdStr))).first();

                if (userDoc != null) {
                    User user = new User();
                    user.setId(userDoc.getObjectId("_id").toString());
                    user.setUsername(userDoc.getString("username"));
                    user.setEmail(userDoc.getString("email"));
                    user.setFullName(userDoc.getString("fullName"));
                    user.setExchangeCount(entry.getValue());
                    users.add(user);
                    count++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return users;
    }

    @Override
    public int getTotalExchanges() {
        MongoCollection<Document> exchanges = database.getCollection("exchanges");
        return (int) exchanges.countDocuments();
    }

    @Override
    public int getTotalBooks() {
        MongoCollection<Document> books = database.getCollection("books");
        return (int) books.countDocuments();
    }

    @Override
    public int getTotalUsers() {
        MongoCollection<Document> users = database.getCollection("users");
        return (int) users.countDocuments();
    }
}
