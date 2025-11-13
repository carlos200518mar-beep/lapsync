package org.esfe.servicios.implementaciones;

import org.esfe.modelos.Penalty;
import org.esfe.modelos.User;
import org.esfe.repositorios.IPenaltyRepository;
import org.esfe.servicios.interfaces.IPenaltyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PenaltyServiceImpl implements IPenaltyService {

    @Autowired
    private IPenaltyRepository penaltyRepository;

    @Override
    public Penalty guardar(Penalty penalty) {
        if (penalty.getCreatedAt() == null) {
            penalty.setCreatedAt(LocalDateTime.now());
        }
        return penaltyRepository.save(penalty);
    }

    @Override
    public List<Penalty> listarTodas() {
        return penaltyRepository.findAll();
    }

    @Override
    @SuppressWarnings("null")
    public Optional<Penalty> buscarPorId(Integer id) {
        return penaltyRepository.findById(id);
    }

    @Override
    @SuppressWarnings("null")
    public void eliminar(Integer id) {
        penaltyRepository.deleteById(id);
    }

    @Override
    public List<Penalty> buscarPorUsuario(User user) {
        return penaltyRepository.findByUser(user);
    }

    @Override
    public List<Penalty> buscarPorUsuarioId(Integer userId) {
        return penaltyRepository.findByUserId(userId);
    }

    @Override
    public List<Penalty> buscarActivas() {
        return penaltyRepository.findByIsResolvedFalse();
    }

    @Override
    public List<Penalty> buscarResueltas() {
        return penaltyRepository.findByIsResolvedTrue();
    }

    @Override
    public List<Penalty> buscarPorTipo(String type) {
        return penaltyRepository.findByType(type);
    }

    @Override
    public List<Penalty> buscarActivasPorUsuario(Integer userId) {
        return penaltyRepository.findActivePenaltiesByUserId(userId);
    }

    @Override
    @SuppressWarnings("null")
    public Penalty resolverSancion(Integer penaltyId) {
        Optional<Penalty> penaltyOpt = penaltyRepository.findById(penaltyId);
        if (penaltyOpt.isPresent()) {
            Penalty penalty = penaltyOpt.get();
            penalty.setIsResolved(true);
            penalty.setResolvedAt(LocalDateTime.now());
            return penaltyRepository.save(penalty);
        }
        throw new RuntimeException("Sanción no encontrada con ID: " + penaltyId);
    }

    @Override
    public boolean tienesSancionesActivas(Integer userId) {
        return contarSancionesActivas(userId) > 0;
    }

    @Override
    public Long contarSancionesActivas(Integer userId) {
        return penaltyRepository.countActivePenaltiesByUserId(userId);
    }

    @Override
    public Penalty crearSancionDanioFisico(User user, String descripcion, Double montoMulta) {
        Penalty penalty = new Penalty();
        penalty.setUser(user);
        penalty.setType("Daño físico");
        penalty.setDescription(descripcion);
        penalty.setFineAmount(BigDecimal.valueOf(montoMulta));
        penalty.setIsResolved(false);
        penalty.setCreatedAt(LocalDateTime.now());
        return guardar(penalty);
    }

    @Override
    public Penalty crearSancionExcesoTiempo(User user, String descripcion) {
        Penalty penalty = new Penalty();
        penalty.setUser(user);
        penalty.setType("Exceso de tiempo");
        penalty.setDescription(descripcion);
        penalty.setFineAmount(BigDecimal.ZERO);
        penalty.setIsResolved(false);
        penalty.setCreatedAt(LocalDateTime.now());
        return guardar(penalty);
    }

    @Override
    public Penalty crearSancionSacarEquipo(User user, String descripcion, Double montoMulta) {
        Penalty penalty = new Penalty();
        penalty.setUser(user);
        penalty.setType("Sacar equipo fuera de institución");
        penalty.setDescription(descripcion);
        penalty.setFineAmount(BigDecimal.valueOf(montoMulta));
        penalty.setIsResolved(false);
        penalty.setCreatedAt(LocalDateTime.now());
        return guardar(penalty);
    }
}
