package org.esfe.servicios.implementaciones;

import org.esfe.modelos.DamageReport;
import java.util.List;

/**
 * Servicio para obtener y manejar reportes de daños.
 */
public interface DamageReportService {
    List<DamageReport> getReportsForLaptop(Integer laptopId);
    DamageReport save(DamageReport report);
}
