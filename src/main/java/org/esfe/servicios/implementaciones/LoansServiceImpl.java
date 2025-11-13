package org.esfe.servicios.implementaciones;

import org.esfe.modelos.Loans;
import org.esfe.modelos.User;
import org.esfe.repositorios.ILoansRepository;
import org.esfe.servicios.interfaces.ILoansService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LoansServiceImpl implements ILoansService {
    private final ILoansRepository loansRepository;

    public LoansServiceImpl(ILoansRepository loansRepository) {
        this.loansRepository = loansRepository;
    }

    @Override
    public Loans save(Loans loan) {
        // Verificar cambios de estado y asignar fechas
        switch (loan.getStatus()) {
            // Prestamo es aprobado
            case "approved":
                if (loan.getApprovedAt() == null) {
                    loan.setApprovedAt(LocalDateTime.now());
                }
                break;
                // Prestamo es activo(en curso)
            case "active":
                if (loan.getDeliveredAt() == null) {
                    loan.setDeliveredAt(LocalDateTime.now());
                }
                //  marcar laptop como "loaned"
                if (loan.getLaptop() != null) {
                    loan.getLaptop().setStatus("loaned");
                }
                break;
                // Prestamo es marcado como completado
            case "completed":
                if (loan.getReturnedAt() == null) {
                    loan.setReturnedAt(LocalDateTime.now());
                }
                //  marcar laptop como "available"
                if (loan.getLaptop() != null) {
                    loan.getLaptop().setStatus("available");
                }
                break;
        }
        // Guardar cambios
        return loansRepository.save(loan);
    }

    @Override
    public List<Loans> listarTodos() {
        return loansRepository.findAll();
    }

    @Override
    @SuppressWarnings("null")
    public Loans buscarPorId(Integer id) {
        return loansRepository.findById(id).orElse(null);
    }

    @Override
    public List<Loans> buscarPorUsuario(User user){
        return loansRepository.findByUser(user);
    }

    @Override
    public List<Loans> listarTodosOrdenadosPorFechaDesc() {
        return loansRepository.findAllOrderByRequestedAtDesc();
    }

    @Override
    public List<Loans> buscarPorUsuarioOrdenados(User user) {
        return loansRepository.findByUserOrderByRequestedAtDesc(user);
    }
}
