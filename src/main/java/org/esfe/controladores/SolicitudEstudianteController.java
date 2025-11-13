package org.esfe.controladores;

import org.esfe.modelos.Loans;
import org.esfe.modelos.Laptop;
import org.esfe.modelos.User;
import org.esfe.servicios.interfaces.ILoansService;
import org.esfe.servicios.interfaces.IUserService;
import org.esfe.servicios.implementaciones.LaptopService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/solicitudes-usuario")
public class SolicitudEstudianteController {

    private final ILoansService loansService;
    private final IUserService userService;
    private final LaptopService laptopsService;

    // Inyeccion de dependencia mediante constructor
    public SolicitudEstudianteController(ILoansService loansService, IUserService userService, LaptopService laptopsService) {
        this.loansService = loansService;
        this.userService = userService;
        this.laptopsService = laptopsService;
    }

    // Mostrar préstamos del usuario
    @GetMapping("/{userId}")
    public String verPrestamosUsuario(@PathVariable Integer userId, Model model) {
        // Buscar al usuario en la base de datos por su Id
        User user = userService.buscarPorId(userId).orElse(null);
        if (user == null) return "redirect:/solicitudes"; // Si no existe, redirigir al listado

        //Ordenar solicitudes por orden desendente usando su fecha
        List<Loans> prestamos = loansService.buscarPorUsuario(user)
                .stream()
                .sorted((p1,p2) -> p2.getRequestedAt().compareTo(p1.getRequestedAt()))
                .toList();

        //Filtrar laptops con estado disponible
        List<Laptop> laptops = laptopsService.getAllLaptops()
                .stream()
                .filter(laptop -> "available".equalsIgnoreCase(laptop.getStatus()))
                .toList();

        // Pasar los datos a la vista
        model.addAttribute("prestamos", prestamos);
        model.addAttribute("user", user);
        model.addAttribute("laptops", laptops);

        return "Administrador/solicitudEstudiante";
    }

    // Actualizar estado o asignar laptop
    @PostMapping("/update/{loanId}")
    public String actualizarPrestamo(@PathVariable Integer loanId,
                                     @RequestParam String action,
                                     @RequestParam(required = false) Integer laptopId) {
        // Buscar el prestamo en la base de datos
        Loans loan = loansService.buscarPorId(loanId);
        if (loan == null) return "redirect:/solicitudes";

        // Acciones a realizar según el parametro permitido
        switch (action) {
            case "approved" -> {
                // Aprobar prestamo
                loan.setStatus("approved");
                loan.setApprovedAt(LocalDateTime.now());
            }
            case "active" -> {
                // Marcar prestamo como activo
                loan.setStatus("active");
                loan.setDeliveredAt(LocalDateTime.now());
                //Cambiar estado de la computadora
                if (loan.getLaptop() != null){
                    loan.getLaptop().setStatus("loaned");
                }
            }
            case "completed" -> {
                // Marcar prestamo como finalizado
                loan.setStatus("completed");
                loan.setReturnedAt(LocalDateTime.now());
                //Cambiar estado de la computadora
                if(loan.getLaptop() != null){
                    loan.getLaptop().setStatus("available");
                }
            }
            // Rechazar solicitud
            case "rejected" -> {
                loan.setStatus("rejected");
            }
            case "assign" -> {
                // Solo se puede asignar laptop si el estado es approved
                if ("approved".equals(loan.getStatus()) && laptopId != null) {
                    Laptop laptop = laptopsService.getLaptopById(laptopId).orElse(null);
                    loan.setLaptop(laptop);
                }
            }
        }
        // Guardar cambios en el prestamo
        loansService.save(loan);
        return "redirect:/solicitudes-usuario/" + loan.getUser().getId();
    }
}
