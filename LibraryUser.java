import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
