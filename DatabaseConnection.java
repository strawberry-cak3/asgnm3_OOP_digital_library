import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
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