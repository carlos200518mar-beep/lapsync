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
        return "home/index"; // Esto buscará templates/home/index.html
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
