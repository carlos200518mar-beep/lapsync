package org.esfe.controladores;

import org.esfe.modelos.Penalty;
import org.esfe.modelos.User;
import org.esfe.servicios.interfaces.IPenaltyService;
import org.esfe.servicios.interfaces.IUserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/detalleEstudiante")
public class DetalleEstudianteController {

    private final IUserService userService;
    private final IPenaltyService penaltyService;

    public DetalleEstudianteController(IUserService userService, IPenaltyService penaltyService) {
        this.userService = userService;
        this.penaltyService = penaltyService;
    }

    @GetMapping("/{userId}")
    public String verDetalleEstudiante(@PathVariable Integer userId, Model model) {
        Optional<User> userOpt = userService.buscarPorId(userId);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            List<Penalty> sanciones = penaltyService.buscarPorUsuarioId(user.getId());
            List<Penalty> sancionesActivas = penaltyService.buscarActivasPorUsuario(user.getId());

            sanciones.sort(Comparator.comparing(Penalty::getCreatedAt).reversed());

            model.addAttribute("sanciones", sanciones);
            model.addAttribute("sancionesActivas", sancionesActivas);
            model.addAttribute("usuario", user);
            model.addAttribute("titulo", "Detalle del Estudiante");
        }

        return "Administrador/detalleEstudiante";
    }
}
