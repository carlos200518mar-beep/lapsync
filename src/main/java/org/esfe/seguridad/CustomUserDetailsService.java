package org.esfe.seguridad;

import org.esfe.modelos.User;
import org.esfe.repositorios.IUserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final IUserRepository userRepository;

    public CustomUserDetailsService(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User u = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con el correo: " + email));

        // En BD guardas: "student" | "admin" | "superadmin"
        String role = (u.getRole() == null ? "STUDENT" : u.getRole().toUpperCase());
        List<GrantedAuthority> auths = List.of(new SimpleGrantedAuthority("ROLE_" + role));

        boolean enabled = u.getIsActive() == null || Boolean.TRUE.equals(u.getIsActive());
        String encodedPassword = (u.getPasswordHash() == null) ? "" : u.getPasswordHash();

        return org.springframework.security.core.userdetails.User
                .withUsername(u.getEmail())
                .password(encodedPassword)
                .authorities(auths)
                .accountExpired(false)
                .accountLocked(!enabled)  // si no está activo, lo consideramos bloqueado
                .credentialsExpired(false)
                .disabled(!enabled)
                .build();
    }
}
