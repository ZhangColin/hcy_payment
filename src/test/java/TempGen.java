import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TempGen {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
        String hash = encoder.encode("C@rt1s@n");
        System.out.println(hash);
        System.out.println("\n验证测试:");
        System.out.println(encoder.matches("C@rt1s@n", hash));
    }
}
