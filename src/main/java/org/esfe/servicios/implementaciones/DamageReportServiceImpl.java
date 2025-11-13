package org.esfe.servicios.implementaciones;

import org.esfe.modelos.DamageReport;
import org.esfe.modelos.Laptop;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DamageReportServiceImpl implements DamageReportService {

    @Autowired
    private LaptopService laptopService;

    @Override
    public List<DamageReport> getReportsForLaptop(Integer laptopId) {
        Laptop laptop = laptopService.getLaptopById(laptopId).orElse(null);
        if (laptop == null || laptop.getConditionDescription() == null || laptop.getConditionDescription().isBlank()) {
            return List.of(); // no hay daños registrados
        }
        return List.of(
                new DamageReport(
                        laptop.getConditionDescription(),
                        laptop.getUpdatedAt() != null ? laptop.getUpdatedAt() : LocalDateTime.now(),
                        laptop
                )
        );
    }

    @Override
    public DamageReport save(DamageReport report) {
        // No guardamos en BD, solo devolvemos el reporte
        return report;
    }
}
