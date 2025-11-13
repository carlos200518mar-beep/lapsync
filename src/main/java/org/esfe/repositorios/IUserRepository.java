package org.esfe.repositorios;

import org.esfe.modelos.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import java.util.Optional;

public interface IUserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
    boolean existsById(@NonNull Integer id);
}
