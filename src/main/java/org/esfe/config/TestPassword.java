
package org.esfe.config;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


public class TestPassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String rawPassword = "admin123"; // o la que creés que usaste
        String encodedFromDB = "$2a$10$jCDhyWdUJzSZRGU5pq.hVubUs/vMNgUim4rMnX1SWBo0q/APj5U5O";

        boolean matches = encoder.matches(rawPassword, encodedFromDB);
        System.out.println("¿La contraseña coincide? " + matches);
    }
}
