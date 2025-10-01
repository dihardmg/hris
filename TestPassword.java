import java.sql.*;

public class TestPassword {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/hris";
        String user = "hris_user";
        String password = "hris_password";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            // Get the stored password hash
            String sql = "SELECT email, password FROM employees WHERE email = 'employee@hris.com'";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                if (rs.next()) {
                    String email = rs.getString("email");
                    String storedHash = rs.getString("password");

                    System.out.println("Email: " + email);
                    System.out.println("Stored hash: " + storedHash);
                    System.out.println("Password to test: admin123");

                    // Test different possible BCrypt hashes
                    String[] possibleHashes = {
                        "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9b2.2L.4f9i8H8u", // Original from V6
                        "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi", // Common test hash
                        "$2a$10$rKZy9sHvN7Jb8dJQW6uT6OJ9sNqGwL5xK9yJQ8mNkL3pW2rHcOj2"  // Another test hash
                    };

                    for (String testHash : possibleHashes) {
                        System.out.println("\nTesting hash: " + testHash);
                        System.out.println("Matches stored hash: " + testHash.equals(storedHash));
                    }
                } else {
                    System.out.println("Employee not found");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}