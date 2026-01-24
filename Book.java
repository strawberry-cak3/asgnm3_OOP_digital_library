import java.time.Year;
import java.util.Objects;

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