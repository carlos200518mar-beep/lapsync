package org.esfe.repositorios;

import org.esfe.modelos.Penalty;
import org.esfe.modelos.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IPenaltyRepository extends JpaRepository<Penalty, Integer> {
    long countByIsResolvedFalse();

    // Buscar penalizaciones por usuario
    List<Penalty> findByUser(User user);

    // Buscar penalizaciones por usuario ID
    List<Penalty> findByUserId(Integer userId);

    // Buscar penalizaciones activas (no resueltas)
    List<Penalty> findByIsResolvedFalse();

    // Buscar penalizaciones resueltas
    List<Penalty> findByIsResolvedTrue();

    // Buscar penalizaciones por tipo
    List<Penalty> findByType(String type);

    // Buscar penalizaciones activas por usuario
    @Query("SELECT p FROM Penalty p WHERE p.user.id = :userId AND p.isResolved = false")
    List<Penalty> findActivePenaltiesByUserId(@Param("userId") Integer userId);

    // Contar penalizaciones activas por usuario
    @Query("SELECT COUNT(p) FROM Penalty p WHERE p.user.id = :userId AND p.isResolved = false")
    Long countActivePenaltiesByUserId(@Param("userId") Integer userId);
}
