package org.esfe.controladores;

import org.esfe.modelos.Loans;
import org.esfe.modelos.User;
import org.esfe.servicios.interfaces.ILoansService;
import org.esfe.servicios.interfaces.IUserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class MisPrestamosController {
    private final ILoansService loansService;
    private final IUserService userService;

    // Inyeccion de dependencias mediante constructor
    public MisPrestamosController(ILoansService loansService, IUserService userService) {
        this.loansService = loansService;
        this.userService = userService;
    }

    @GetMapping("/estudiante/prestamos")
    public String verMisPrestamos(Model model, @AuthenticationPrincipal OAuth2User principal) {
        // Si no hay usuario autenticado redirige al login
        if (principal == null) {
            return "redirect:/login";
        }

        // Obtener el usuario logueado mediante su email
        String email = principal.getAttribute("email");
        Optional<User> userOptional = userService.buscarPorEmail(email);

        // Si el usuario no existe en la base de datos
        if (userOptional.isEmpty()) {
            model.addAttribute("loans", null); // No tiene préstamos
            return "Estudiante/prestamos"; // Muestra vista vacia
        }

        // buscar últimos préstamos de ese usuario
        List<Loans> prestamos = loansService.buscarPorUsuario(userOptional.get());

        // Ordenar los préstamos por fecha de solicitud (más reciente primero)
        prestamos = prestamos.stream()
                .sorted(Comparator.comparing(Loans::getRequestedAt).reversed())
                .collect(Collectors.toList());

        // Agregar la lista de préstamos al modelo para mostrarlos en la vista
        model.addAttribute("loans", prestamos);
        // Agregar datos del usuario autenticado
        model.addAttribute("nombre", principal.getAttribute("name"));
        model.addAttribute("email", principal.getAttribute("email"));
        model.addAttribute("foto", principal.getAttribute("picture"));
        return "Estudiante/prestamos";
    }
}
