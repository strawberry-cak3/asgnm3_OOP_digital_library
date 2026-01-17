import java.time.LocalDate;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;  // Для динамических запросов, как в лекции
import java.sql.Date;

public class Program { // digital library system

}

/**
 * Represents a book in the digital library
 */
class Book {
    private final String isbn;           // natural unique key
    private final String title;
    private final String author;
    private final Year publicationYear;
    private final String genre;
    boolean available = true;

    public Book(String isbn, String title, String author,
                Year publicationYear, String genre) {
        this.isbn = Objects.requireNonNull(isbn, "ISBN cannot be null");
        this.title = Objects.requireNonNull(title, "Title cannot be null");
        this.author = Objects.requireNonNull(author, "Author cannot be null");
        this.publicationYear = Objects.requireNonNull(publicationYear);
        this.genre = genre != null ? genre : "Unknown";
    }

    // Getters (no setters → mostly immutable except availability)
    public String getIsbn() { return isbn; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public Year getPublicationYear() { return publicationYear; }
    public String getGenre() { return genre; }
    public boolean isAvailable() { return available; }

    // Business methods
    public void borrow() {
        if (!available) {
            throw new IllegalStateException("Book is already borrowed: " + title);
        }
        this.available = false;
    }

    public void returnBook() {
        this.available = true;
    }

    @Override
    public String toString() {
        return String.format("Book{isbn='%s', title='%s', author='%s', year=%s, genre='%s', available=%b}",
                isbn, title, author, publicationYear, genre, available);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return isbn.equals(book.isbn);
    }

    @Override
    public int hashCode() {
        return isbn.hashCode();
    }
}

/**
 * Base class for all kinds of library users
 */
abstract class LibraryUser {
    protected final String id;          // unique identifier
    protected final String name;
    protected final LocalDate registrationDate;
    protected final List<Book> borrowedBooks = new ArrayList<>();

    public LibraryUser(String id, String name, LocalDate registrationDate) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.registrationDate = Objects.requireNonNull(registrationDate, "Registration date cannot be null");
    }

    public LibraryUser(String id, String name) {
        this(id, name, LocalDate.now());
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public LocalDate getRegistrationDate() { return registrationDate; }
    public List<Book> getBorrowedBooks() {
        return Collections.unmodifiableList(borrowedBooks);
    }

    public abstract int getMaxBooksAllowed();
    public abstract int getLoanPeriodDays();

    public boolean canBorrowMore() {
        return borrowedBooks.size() < getMaxBooksAllowed();
    }

    public void borrowBook(Book book) {
        if (!canBorrowMore()) {
            throw new IllegalStateException(name + " has reached the maximum number of books ("
                    + getMaxBooksAllowed() + ")");
        }
        book.borrow();
        borrowedBooks.add(book);
    }

    public void returnBook(Book book) {
        if (borrowedBooks.remove(book)) {
            book.returnBook();
        } else {
            throw new IllegalArgumentException("This user didn't borrow book: " + book.getTitle());
        }
    }

    @Override
    public String toString() {
        return String.format("%s{id='%s', name='%s', registered=%s, borrowed=%d books}",
                getClass().getSimpleName(), id, name, registrationDate, borrowedBooks.size());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LibraryUser that = (LibraryUser) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}

/**
 * Regular (student/ordinary) library user
 */
class RegularUser extends LibraryUser {
    public RegularUser(String id, String name, LocalDate registrationDate) {
        super(id, name, registrationDate);
    }

    public RegularUser(String id, String name) {
        super(id, name);
    }

    @Override
    public int getMaxBooksAllowed() {
        return 5;
    }

    @Override
    public int getLoanPeriodDays() {
        return 14;
    }
}

/**
 * Premium / Teacher / Researcher user with more privileges
 */
class PremiumUser extends LibraryUser {
    public PremiumUser(String id, String name, LocalDate registrationDate) {
        super(id, name, registrationDate);
    }

    public PremiumUser(String id, String name) {
        super(id, name);
    }

    @Override
    public int getMaxBooksAllowed() {
        return 15;
    }

    @Override
    public int getLoanPeriodDays() {
        return 30;
    }
}

/**
 * Main class - Digital Library
 */
class Library {
    private final String name;
    private final Map<String, Book> booksByIsbn = new HashMap<>();
    private final Map<String, LibraryUser> usersById = new HashMap<>();

    public Library(String name) {
        this.name = name;
    }

    // ── Book operations ───────────────────────────────────────
    public void addBook(Book book) {
        booksByIsbn.put(book.getIsbn(), book);
    }

    public Optional<Book> findBookByIsbn(String isbn) {
        return Optional.ofNullable(booksByIsbn.get(isbn));
    }

    public List<Book> findBooksByTitleContains(String fragment) {
        String lower = fragment.toLowerCase();
        return booksByIsbn.values().stream()
                .filter(b -> b.getTitle().toLowerCase().contains(lower))
                .collect(Collectors.toList());
    }

    public List<Book> findBooksByAuthor(String author) {
        String normAuthor = author.toLowerCase();
        return booksByIsbn.values().stream()
                .filter(b -> b.getAuthor().toLowerCase().equals(normAuthor))
                .collect(Collectors.toList());
    }

    public List<Book> getAllAvailableBooks() {
        return booksByIsbn.values().stream()
                .filter(Book::isAvailable)
                .sorted(Comparator.comparing(Book::getTitle))
                .collect(Collectors.toList());
    }

    public List<Book> getTopNewestBooks(int limit) {
        return booksByIsbn.values().stream()
                .sorted(Comparator.comparing(Book::getPublicationYear).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    // ── User operations ───────────────────────────────────────
    public void registerUser(LibraryUser user) {
        if (usersById.containsKey(user.getId())) {
            throw new IllegalArgumentException("User with id " + user.getId() + " already exists");
        }
        usersById.put(user.getId(), user);
    }

    public Optional<LibraryUser> findUser(String id) {
        return Optional.ofNullable(usersById.get(id));
    }

    // Quick demo / test
    public static void main(String[] args) {
        // Test database connection
        DatabaseConnection.testConnection();

        // Create DAOs
        BookDAO bookDAO = new BookDAO();
        LibraryUserDAO userDAO = new LibraryUserDAO();

        // Demonstrate CRUD for Books
        System.out.println("\n=== Book CRUD Operations ===");

        // Insert books
        Book cleanCode = new Book("978-0132350884", "Clean Code", "Robert C. Martin", Year.of(2008), "Programming");
        bookDAO.insertBook(cleanCode);

        Book refactoring = new Book("978-0321125217", "Refactoring", "Martin Fowler", Year.of(2018), "Programming");
        bookDAO.insertBook(refactoring);

        Book grokking = new Book("978-1617294945", "Grokking Algorithms", "Aditya Bhargava", Year.of(2016), "Algorithms");
        bookDAO.insertBook(grokking);

        Book dune = new Book("978-0553380163", "Dune", "Frank Herbert", Year.of(1965), "Science Fiction");
        bookDAO.insertBook(dune);

        // Get all books
        System.out.println("\nAll Books:");
        List<Book> allBooks = bookDAO.getAllBooks();
        allBooks.forEach(System.out::println);

        // Get book by ISBN
        Book foundBook = bookDAO.getBookByIsbn("978-0132350884");
        if (foundBook != null) {
            System.out.println("\nFound Book: " + foundBook);
        }

        // Update book availability
        bookDAO.updateBookAvailability("978-0132350884", false);
        System.out.println("\nAfter updating availability:");
        allBooks = bookDAO.getAllBooks();
        allBooks.forEach(System.out::println);

        // Delete a book
        bookDAO.deleteBook("978-0553380163");
        System.out.println("\nAfter deleting Dune:");
        allBooks = bookDAO.getAllBooks();
        allBooks.forEach(System.out::println);

        // Demonstrate CRUD for Users
        System.out.println("\n=== User CRUD Operations ===");

        // Insert users
        LibraryUser student = new RegularUser("STU-123", "Anna Kowalska");
        userDAO.insertLibraryUser(student);

        LibraryUser professor = new PremiumUser("PRF-777", "Dr. Jan Nowak");
        userDAO.insertLibraryUser(professor);

        // Get all users
        System.out.println("\nAll Users:");
        List<LibraryUser> allUsers = userDAO.getAllUsers();
        allUsers.forEach(System.out::println);

        // Get user by ID
        LibraryUser foundUser = userDAO.getUserById("STU-123");
        if (foundUser != null) {
            System.out.println("\nFound User: " + foundUser);
        }

        // Update user name
        userDAO.updateUserName("STU-123", "Anna Smith");
        System.out.println("\nAfter updating name:");
        allUsers = userDAO.getAllUsers();
        allUsers.forEach(System.out::println);

        // Delete a user
        userDAO.deleteUser("PRF-777");
        System.out.println("\nAfter deleting professor:");
        allUsers = userDAO.getAllUsers();
        allUsers.forEach(System.out::println);

        // Optional: In-memory operations (as before, not persisted to DB yet)
        Library lib = new Library("Digital Knowledge Hub");
        lib.addBook(cleanCode);
        lib.addBook(dune);
        lib.registerUser(student);
        lib.registerUser(professor);

        student.borrowBook(cleanCode);

        System.out.println("\nIn-memory available books:");
        lib.getAllAvailableBooks().forEach(System.out::println);
    }
}

// Database Connection class (adapted from template)
class DatabaseConnection {
    private static final String URL = "jdbc:postgresql://localhost:5432/digital_library1337";
    private static final String USER = "postgres";
    private static final String PASSWORD = "1488";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void testConnection() {
        try (Connection conn = getConnection()) {
            if (conn != null) {
                System.out.println("Connected to PostgreSQL successfully");
            } else {
                System.out.println("Failed to connect to database");
            }
        } catch (SQLException e) {
            System.err.println("Connection error: " + e.getMessage());
        }
    }
}

// Book DAO for CRUD operations
class BookDAO {

    public void insertBook(Book book) {
        String sql = "INSERT INTO books (isbn, title, author, publication_year, genre, available) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, book.getIsbn());
            pstmt.setString(2, book.getTitle());
            pstmt.setString(3, book.getAuthor());
            pstmt.setInt(4, book.getPublicationYear().getValue());
            pstmt.setString(5, book.getGenre());
            pstmt.setBoolean(6, book.isAvailable());

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Book inserted: " + book.getTitle());
            }

        } catch (SQLException e) {
            System.err.println("Error inserting book: " + e.getMessage());
        }
    }

    public Book getBookByIsbn(String isbn) {
        String sql = "SELECT * FROM books WHERE isbn = ?";
        Book book = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, isbn);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                book = new Book(
                        rs.getString("isbn"),
                        rs.getString("title"),
                        rs.getString("author"),
                        Year.of(rs.getInt("publication_year")),
                        rs.getString("genre")
                );
                // Note: availability is set in constructor to true, but override if needed
                // Since available is not final, we can set it
                book.available = rs.getBoolean("available");  // Assuming we make 'available' accessible or use reflection, but for simplicity, added setter or made non-final. Wait, it's private boolean available = true;
                // To set, need to add setter or make package-visible. For this, let's assume we add:
                // public void setAvailable(boolean available) { this.available = available; }
                // Add it to Book class if needed. For demo, assuming set after creation.
            }

        } catch (SQLException e) {
            System.err.println("Error finding book: " + e.getMessage());
        }

        return book;
    }

    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books ORDER BY title";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Book book = new Book(
                        rs.getString("isbn"),
                        rs.getString("title"),
                        rs.getString("author"),
                        Year.of(rs.getInt("publication_year")),
                        rs.getString("genre")
                );
                book.available = rs.getBoolean("available");  // Same note as above
                books.add(book);
            }

        } catch (SQLException e) {
            System.err.println("Error getting books: " + e.getMessage());
        }

        return books;
    }

    public void updateBookAvailability(String isbn, boolean newAvailable) {
        String sql = "UPDATE books SET available = ? WHERE isbn = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBoolean(1, newAvailable);
            pstmt.setString(2, isbn);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Book ISBN=" + isbn + " availability updated to " + newAvailable);
            } else {
                System.out.println("Book with ISBN=" + isbn + " not found");
            }

        } catch (SQLException e) {
            System.err.println("Error updating availability: " + e.getMessage());
        }
    }

    public void deleteBook(String isbn) {
        String sql = "DELETE FROM books WHERE isbn = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, isbn);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Book ISBN=" + isbn + " deleted");
            } else {
                System.out.println("Book with ISBN=" + isbn + " not found");
            }

        } catch (SQLException e) {
            System.err.println("Error deleting book: " + e.getMessage());
        }
    }
}

// LibraryUser DAO for CRUD operations
class LibraryUserDAO {

    public void insertLibraryUser(LibraryUser user) {
        String sql = "INSERT INTO users (id, name, registration_date, user_type) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getId());
            pstmt.setString(2, user.getName());
            pstmt.setDate(3, Date.valueOf(user.getRegistrationDate()));
            String userType = user instanceof RegularUser ? "regular" : "premium";
            pstmt.setString(4, userType);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("User inserted: " + user.getName());
            }

        } catch (SQLException e) {
            System.err.println("Error inserting user: " + e.getMessage());
        }
    }

    public LibraryUser getUserById(String id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        LibraryUser user = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String name = rs.getString("name");
                LocalDate regDate = rs.getDate("registration_date").toLocalDate();
                String type = rs.getString("user_type");

                if ("regular".equals(type)) {
                    user = new RegularUser(id, name, regDate);
                } else if ("premium".equals(type)) {
                    user = new PremiumUser(id, name, regDate);
                }
                // borrowedBooks not loaded for this milestone
            }

        } catch (SQLException e) {
            System.err.println("Error finding user: " + e.getMessage());
        }

        return user;
    }

    public List<LibraryUser> getAllUsers() {
        List<LibraryUser> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY id";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String id = rs.getString("id");
                String name = rs.getString("name");
                LocalDate regDate = rs.getDate("registration_date").toLocalDate();
                String type = rs.getString("user_type");

                LibraryUser user;
                if ("regular".equals(type)) {
                    user = new RegularUser(id, name, regDate);
                } else if ("premium".equals(type)) {
                    user = new PremiumUser(id, name, regDate);
                } else {
                    continue; // skip invalid
                }
                users.add(user);
            }

        } catch (SQLException e) {
            System.err.println("Error getting users: " + e.getMessage());
        }

        return users;
    }

    public void updateUserName(String id, String newName) {
        String sql = "UPDATE users SET name = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newName);
            pstmt.setString(2, id);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("User ID=" + id + " name updated to " + newName);
            } else {
                System.out.println("User with ID=" + id + " not found");
            }

        } catch (SQLException e) {
            System.err.println("Error updating name: " + e.getMessage());
        }
    }

    public void deleteUser(String id) {
        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("User ID=" + id + " deleted");
            } else {
                System.out.println("User with ID=" + id + " not found");
            }

        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
        }
    }
}