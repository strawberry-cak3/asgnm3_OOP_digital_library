import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
