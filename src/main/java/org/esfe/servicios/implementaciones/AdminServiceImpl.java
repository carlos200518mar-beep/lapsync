package org.esfe.servicios.implementaciones;

import jakarta.transaction.Transactional;
import org.esfe.modelos.User;
import org.esfe.repositorios.IUserRepository;
import org.esfe.servicios.interfaces.IAdminService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminServiceImpl implements IAdminService {

    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminServiceImpl(IUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    @Override
    public void crearAdmin(User admin, String rawPassword) {
        // Normalizaciones útiles
        if (admin.getEmail() != null) admin.setEmail(admin.getEmail().trim().toLowerCase());
        if (admin.getFullName() != null) admin.setFullName(admin.getFullName().trim());

        // Role por defecto + en minúsculas para cumplir el @Pattern
        if (admin.getRole() == null || admin.getRole().isBlank()) {
            admin.setRole("admin");
        } else {
            admin.setRole(admin.getRole().toLowerCase());
        }

        admin.setIsActive(true);
        admin.setPasswordHash(passwordEncoder.encode(rawPassword));

        userRepository.save(admin);
    }

    @Transactional
    @Override
    public void actualizarPassword(User admin, String rawPassword) {
        admin.setPasswordHash(passwordEncoder.encode(rawPassword));
        userRepository.save(admin);
    }
}
