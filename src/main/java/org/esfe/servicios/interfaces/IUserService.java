package org.esfe.servicios.interfaces;

import org.esfe.modelos.User;

import java.util.Optional;

public interface IUserService {
    User guardar (User user);
    Optional<User> buscarPorEmail (String email);
    Optional<User> buscarPorId(Integer id);
}
