package org.esfe.repositorios;

import org.esfe.modelos.Laptop;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LaptopRepository extends JpaRepository<Laptop, Integer> {

    long countByStatus(String status);

    @Query("""
       SELECT l
       FROM Laptop l
       WHERE l.status = :status
       ORDER BY COALESCE(l.updatedAt, l.createdAt) DESC
    """)
    Slice<Laptop> findRepairOrdered(@Param("status") String status, Pageable pageable);

    Optional<Laptop> findByAssetTag(String assetTag);
    boolean existsByAssetTag(String assetTag);
    List<Laptop> findByStatus(String status);
}
