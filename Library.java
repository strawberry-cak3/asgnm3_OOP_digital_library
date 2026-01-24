import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

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
