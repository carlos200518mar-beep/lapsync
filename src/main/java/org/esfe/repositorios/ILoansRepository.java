package org.esfe.repositorios;

import org.esfe.modelos.Loans;
import org.esfe.modelos.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ILoansRepository extends JpaRepository<Loans, Integer> {

    long countByStatus(String status);

    // Últimas solicitudes pendientes (Slice = más ligero que Page)
    Slice<Loans> findByStatusOrderByRequestedAtDesc(String status, Pageable pageable);

    // Útil: préstamos activos sin devolver
    List<Loans> findByStatusAndDeliveredAtIsNotNullAndReturnedAtIsNull(String status);

    // Búsquedas por usuario
    List<Loans> findByUser(User user);

    // Ordenado por fecha para usuario
    @Query("SELECT l FROM Loans l WHERE l.user = :user ORDER BY l.requestedAt DESC")
    List<Loans> findByUserOrderByRequestedAtDesc(@Param("user") User user);

    // Todos ordenados por fecha desc
    @Query("SELECT l FROM Loans l ORDER BY l.requestedAt DESC")
    List<Loans> findAllOrderByRequestedAtDesc();

    // --- KPI: préstamos por vencer entre now y limit (JPQL compatible con todos los BD) ---
    @Query("""
            SELECT COUNT(l) 
            FROM Loans l
            WHERE l.status = 'active'
              AND l.deliveredAt IS NOT NULL
              AND l.returnedAt IS NULL
              AND FUNCTION('TIMESTAMPADD', HOUR, l.requestedHours, l.deliveredAt) BETWEEN :now AND :limit
            """)
    long countDueBetween(@Param("now") LocalDateTime now,
                         @Param("limit") LocalDateTime limit);

    @Query("""
            SELECT l 
            FROM Loans l
            WHERE l.status = 'active'
              AND l.deliveredAt IS NOT NULL
              AND l.returnedAt IS NULL
              AND FUNCTION('TIMESTAMPADD', HOUR, l.requestedHours, l.deliveredAt) BETWEEN :now AND :limit
            """)
    List<Loans> findDueBetween(@Param("now") LocalDateTime now,
                               @Param("limit") LocalDateTime limit);
}
