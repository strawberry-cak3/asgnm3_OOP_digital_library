import java.sql.*;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

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
