package org.esfe.servicios.interfaces;

import org.esfe.modelos.Penalty;
import org.esfe.modelos.User;

import java.util.List;
import java.util.Optional;

public interface IPenaltyService {

    // Operaciones CRUD básicas
    Penalty guardar(Penalty penalty);

    List<Penalty> listarTodas();

    Optional<Penalty> buscarPorId(Integer id);

    void eliminar(Integer id);

    // Operaciones específicas del negocio
    List<Penalty> buscarPorUsuario(User user);

    List<Penalty> buscarPorUsuarioId(Integer userId);

    List<Penalty> buscarActivas();

    List<Penalty> buscarResueltas();

    List<Penalty> buscarPorTipo(String type);

    List<Penalty> buscarActivasPorUsuario(Integer userId);

    // Operaciones de resolución
    Penalty resolverSancion(Integer penaltyId);

    boolean tienesSancionesActivas(Integer userId);

    Long contarSancionesActivas(Integer userId);

    // Crear nuevas sanciones
    Penalty crearSancionDanioFisico(User user, String descripcion, Double montoMulta);

    Penalty crearSancionExcesoTiempo(User user, String descripcion);

    Penalty crearSancionSacarEquipo(User user, String descripcion, Double montoMulta);
}
