package org.esfe.controladores;

import org.esfe.modelos.User;
import org.esfe.repositorios.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/test")
public class TestController {

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Crear usuario administrador de prueba
    @GetMapping("/crear-admin")
    public String crearAdminPrueba(RedirectAttributes redirectAttributes) {
        try {
            // Verificar si ya existe
            if (userRepository.findByEmail("admin@test.com").isPresent()) {
                redirectAttributes.addFlashAttribute("mensaje", "El administrador de prueba ya existe");
                redirectAttributes.addFlashAttribute("tipoMensaje", "info");
                return "redirect:/login";
            }

            User admin = new User();
            admin.setFullName("Administrador Prueba");
            admin.setEmail("admin@test.com");
            admin.setPasswordHash(passwordEncoder.encode("Admin123!"));
            admin.setRole("admin");
            admin.setIsActive(true);

            userRepository.save(admin);

            redirectAttributes.addFlashAttribute("mensaje",
                    "Administrador creado: admin@test.com / Admin123!");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
        }

        return "redirect:/login";
    }

    // Crear usuario superadministrador de prueba
    @GetMapping("/crear-superadmin")
    public String crearSuperAdminPrueba(RedirectAttributes redirectAttributes) {
        try {
            // Verificar si ya existe
            if (userRepository.findByEmail("superadmin@test.com").isPresent()) {
                redirectAttributes.addFlashAttribute("mensaje", "El superadministrador de prueba ya existe");
                redirectAttributes.addFlashAttribute("tipoMensaje", "info");
                return "redirect:/login";
            }

            User superAdmin = new User();
            superAdmin.setFullName("Super Administrador Prueba");
            superAdmin.setEmail("superadmin@test.com");
            superAdmin.setPasswordHash(passwordEncoder.encode("SuperAdmin123!"));
            superAdmin.setRole("superadmin");
            superAdmin.setIsActive(true);

            userRepository.save(superAdmin);

            redirectAttributes.addFlashAttribute("mensaje",
                    "Superadministrador creado: superadmin@test.com / SuperAdmin123!");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
        }

        return "redirect:/login";
    }
}
