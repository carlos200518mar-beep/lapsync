package org.esfe.servicios.interfaces;

import org.esfe.modelos.User;

public interface IAdminService {
    void crearAdmin(User admin, String rawPassword);
    void actualizarPassword(User admin, String rawPassword);
}
