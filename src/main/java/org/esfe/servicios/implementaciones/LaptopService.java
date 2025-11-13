package org.esfe.servicios.implementaciones;
//referencias
import org.esfe.modelos.Laptop;
//utilidades de java
import java.util.List;
import java.util.Optional;

public interface LaptopService {
    // HU23
    Laptop saveLaptop(Laptop laptop);

    // HU24
    Laptop updateLaptop(Integer id, Laptop laptopDetails);

    // HU26
    void deactivateLaptop(Integer id);

    // HU25
    Optional<Laptop> getLaptopById(Integer id);
    List<Laptop> getAllLaptops();
    List<Laptop> getLaptopsByStatus(String status);

    // Extra: Buscar por assetTag
    Optional<Laptop> getLaptopByAssetTag(String assetTag);
    boolean existsByAssetTag(String assetTag);
}