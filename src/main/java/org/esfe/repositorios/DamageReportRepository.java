package org.esfe.repositorios;

import org.esfe.modelos.DamageReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DamageReportRepository extends JpaRepository<DamageReport, Long> {

    // Ejemplo: buscar reportes por laptop
    List<DamageReport> findByLaptopId(Long laptopId);
}
