package org.esfe.servicios.interfaces;

import org.esfe.modelos.User;

import java.util.Optional;

public interface IEstudianteService {
    Optional<User> buscarPorEmail(String email);
}