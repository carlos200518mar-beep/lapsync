package org.esfe.controladores;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.esfe.modelos.Loans;
import org.esfe.servicios.interfaces.ILoansService;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("solicitudes")
public class SolicitudesController {
    private final ILoansService loansService;
    // Inyeccion de dependencia mediante constructor
    public SolicitudesController(ILoansService loansService) {
        this.loansService = loansService;
    }

    @GetMapping
    public String listarUsuarios(Model model) {
        // Obtener prestamos ordenados de forma desendente mediante fecha
        List<Loans> loans = loansService.listarTodosOrdenadosPorFechaDesc();

        // Mantener solo el préstamo más reciente por usuario
        Map<Integer, Loans> usuariosRecientes = new LinkedHashMap<>();
        for (Loans loan : loans) {
            Integer userId = loan.getUser().getId(); // Identificar al usuario dueño del préstamo
            if (!usuariosRecientes.containsKey(userId)) {
                // Si todavía no se agregó este usuario, guardar su préstamo más reciente
                usuariosRecientes.put(userId, loan);
            }
        }
        // Pasar la lista de los préstamos más recientes de cada usuario a la vista
        model.addAttribute("usuarios", usuariosRecientes.values());
        return "Administrador/solicitudes";
    }
}