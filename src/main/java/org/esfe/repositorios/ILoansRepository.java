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

    // --- KPI: préstamos por vencer entre now y limit (SQL Server nativo) ---
    @Query(value = """
            SELECT COUNT(*) 
            FROM loans 
            WHERE status = 'active'
              AND delivered_at IS NOT NULL
              AND returned_at IS NULL
              AND DATEADD(HOUR, requested_hours, delivered_at) BETWEEN :now AND :limit
            """, nativeQuery = true)
    long countDueBetween(@Param("now") LocalDateTime now,
                         @Param("limit") LocalDateTime limit);

    @Query(value = """
            SELECT l.* 
            FROM loans l
            WHERE l.status = 'active'
              AND l.delivered_at IS NOT NULL
              AND l.returned_at IS NULL
              AND DATEADD(HOUR, l.requested_hours, l.delivered_at) BETWEEN :now AND :limit
            """, nativeQuery = true)
    List<Loans> findDueBetween(@Param("now") LocalDateTime now,
                               @Param("limit") LocalDateTime limit);
}
