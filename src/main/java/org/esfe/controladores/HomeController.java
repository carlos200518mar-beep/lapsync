package org.esfe.controladores;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // Si el usuario está autenticado y no es anónimo
        if (auth != null && auth.isAuthenticated() 
            && auth.getAuthorities().stream()
                .noneMatch(a -> a.getAuthority().equals("ROLE_ANONYMOUS"))) {
            
            // Obtener el rol
            String role = auth.getAuthorities().iterator().next().getAuthority();
            
            // Redirigir según el rol
            if (role.contains("ADMIN") || role.contains("SUPERADMIN")) {
                return "redirect:/administrador/dashboard";
            } else if (role.contains("STUDENT")) {
                return "redirect:/estudiante/inicio";
            }
        }
        
        // Si no está autenticado, redirigir al login
        return "redirect:/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String role = auth.getAuthorities().iterator().next().getAuthority();

        // Remover el prefijo ROLE_ si existe
        if (role.startsWith("ROLE_")) {
            role = role.substring(5);
        }

        model.addAttribute("userRole", role);
        model.addAttribute("userName", auth.getName());

        return "dashboard/index";
    }
}
