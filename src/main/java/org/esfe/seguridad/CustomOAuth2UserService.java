package org.esfe.seguridad;

import org.esfe.modelos.User;
import org.esfe.repositorios.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired private IUserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        String name  = oAuth2User.getAttribute("name");

        if (email == null) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("missing_email"),
                    "El proveedor no devolvió un email válido"
            );
        }

        if (!email.toLowerCase().endsWith("@esfe.agape.edu.sv")) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("invalid_domain"),
                    "Solo se permiten correos institucionales @esfe.agape.edu.sv"
            );
        }

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User u = new User();
            u.setFullName(name);
            u.setEmail(email);
            u.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
            u.setRole("student");
            u.setIsActive(true);
            u.setCreatedAt(LocalDateTime.now());
            return userRepository.save(u);
        });

        String role = "ROLE_" + user.getRole().trim().toUpperCase();
        return new DefaultOAuth2User(
                Collections.singleton(() -> role),
                oAuth2User.getAttributes(),
                "email"
        );
    }
}
