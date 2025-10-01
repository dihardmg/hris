import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestBCrypt {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "admin123";

        // Test the hash we used in migration
        String ourHash = "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi";
        boolean matches = encoder.matches(password, ourHash);

        System.out.println("Password: " + password);
        System.out.println("Hash: " + ourHash);
        System.out.println("Matches: " + matches);

        // Generate a fresh hash for comparison
        String freshHash = encoder.encode(password);
        System.out.println("Fresh hash: " + freshHash);
        System.out.println("Fresh matches: " + encoder.matches(password, freshHash));
    }
}