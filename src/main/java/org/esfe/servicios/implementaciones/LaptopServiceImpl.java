package org.esfe.servicios.implementaciones;

import org.esfe.modelos.Laptop;
import org.esfe.repositorios.LaptopRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class LaptopServiceImpl implements LaptopService {

    @Autowired
    private LaptopRepository laptopRepository;

    // HU23: Guardar nueva laptop
    @Override
    public Laptop saveLaptop(Laptop laptop) {
        laptop.setCreatedAt(LocalDateTime.now());
        laptop.setUpdatedAt(LocalDateTime.now());
        return laptopRepository.save(laptop);
    }

    // HU24: Actualizar laptop
    @Override
    @SuppressWarnings("null")
    public Laptop updateLaptop(Integer id, Laptop laptopDetails) {
        return laptopRepository.findById(id).map(laptop -> {
            laptop.setAssetTag(laptopDetails.getAssetTag());
            laptop.setBrand(laptopDetails.getBrand());
            laptop.setModel(laptopDetails.getModel());
            laptop.setStatus(laptopDetails.getStatus());
            laptop.setConditionDescription(laptopDetails.getConditionDescription());
            laptop.setUpdatedAt(LocalDateTime.now());
            return laptopRepository.save(laptop);
        }).orElseThrow(() -> new IllegalArgumentException("Laptop no encontrada con id: " + id));
    }

    // HU26: Desactivar (marcar fuera de uso)
    @Override
    @SuppressWarnings("null")
    public void deactivateLaptop(Integer id) {
        laptopRepository.findById(id).ifPresent(laptop -> {
            laptop.setStatus("retired");
            laptop.setUpdatedAt(LocalDateTime.now());
            laptopRepository.save(laptop);
        });
    }

    // HU25: Consultar laptop por ID
    @Override
    @SuppressWarnings("null")
    public Optional<Laptop> getLaptopById(Integer id) {
        return laptopRepository.findById(id);
    }

    // HU25: Consultar todas las laptops
    @Override
    public List<Laptop> getAllLaptops() {
        return laptopRepository.findAll();
    }

    // HU25: Consultar laptops por estado
    @Override
    public List<Laptop> getLaptopsByStatus(String status) {
        return laptopRepository.findByStatus(status);
    }

    // Extra: Buscar por Asset Tag
    @Override
    public Optional<Laptop> getLaptopByAssetTag(String assetTag) {
        return laptopRepository.findByAssetTag(assetTag);
    }

    @Override
    public boolean existsByAssetTag(String assetTag) {
        return laptopRepository.existsByAssetTag(assetTag);
    }
}