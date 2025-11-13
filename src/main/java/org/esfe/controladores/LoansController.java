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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Controller
public class LoansController {
    private final ILoansService loansService;
    private final IUserService userService;

    // Inyectar los servicios por constructor
    public LoansController(ILoansService loansService, IUserService userService) {
        this.loansService = loansService;
        this.userService = userService;
    }

    @GetMapping("/loans")
    public String mostrarFormulario(Model model, @AuthenticationPrincipal OAuth2User principal) {
        Loans loan = new Loans();
        User user = new User();

        // verificar Si hay un usuario autenticado con OAuth2
        if (principal != null) {
            String email = principal.getAttribute("email");

            // Buscar usuario en la base mediante email
            Optional<User> optionalUser = userService.buscarPorEmail(email);
            if (optionalUser.isPresent()) {
                user = optionalUser.get(); // Traer datos existentes y llenar el formulario
            } else {
                // si no existe crear Usuario nuevo desde Google
                user.setFullName(principal.getAttribute("name"));
                user.setEmail(email);
            }
        }
        // Asocia el usuario con el prestamo realizado
        loan.setUser(user);
        // Mostrar objetos y atributos a la vista del formulario
        model.addAttribute("loan", loan);
        model.addAttribute("titulo", "Formulario de Préstamo");

        return "loans/form";
    }

    @PostMapping("/loans/save")
    public String guardarPrestamo(@ModelAttribute("loan") Loans loan, Model model, RedirectAttributes redirectAttributes, @AuthenticationPrincipal OAuth2User principal) {
        // Obtener el usuario que viene del formulario
        User formUser = loan.getUser();

        // Traer al usuario desde la base de datos usando su email
        Optional<User> optionalUser = userService.buscarPorEmail(formUser.getEmail());
        User userPersisted;

        if (optionalUser.isPresent()) {
            // Usuario ya existe → actualizar los datos que vienen del formulario
            userPersisted = optionalUser.get();
            userPersisted.setStudentId(formUser.getStudentId());
            userPersisted.setCareer(formUser.getCareer());
            userPersisted.setNationalId(formUser.getNationalId());
        } else {
            // Usuario no existe → guardarlo en la DB
            userPersisted = userService.guardar(formUser);
        }

        // Verificar préstamos pendientes
        List<Loans> userLoans = loansService.buscarPorUsuario(userPersisted);
        if (!userLoans.isEmpty()) {
            // Buscar el ultimoo prestamo registrado
            Loans lastLoan = userLoans.stream()
                    .max(Comparator.comparing(Loans::getId))
                    .orElse(null);

            // Verificamos si el ultimo prestamo sigue activo
            if (lastLoan != null &&
                    !"completed".equals(lastLoan.getStatus()) && // Prestamo no completado
                    !"rejected".equals(lastLoan.getStatus())) { // Prestamo no rechazado
                model.addAttribute("warningMessage",
                        "❌ No puedes solicitar un nuevo préstamo hasta que tu solicitud anterior esté completada.");
                loan.setUser(userPersisted); // mantener datos en el formulario
                model.addAttribute("loan", loan);
                return "loans/form"; // Renderiza el mismo formulario
            }
        }

        // Asignar el usuario al préstamo
        loan.setUser(userPersisted);

        // Estado inicial de la solicitud
        loan.setStatus("pending");

        //Guardar hora de solicitud automaticamente
        loan.setRequestedAt(LocalDateTime.now());

        // Guardar prestamo en la base de datos
        loansService.save(loan);

        // Mensaje de éxito
        redirectAttributes.addFlashAttribute("successMessage", "✅ Préstamo guardado correctamente");


        // Redirigir después de guardar
        return "redirect:/estudiante/inicio";
    }
}

