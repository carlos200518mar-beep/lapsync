package org.esfe.controladores;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String loginPage() {
        // Retorna la vista login.html en /templates/Auth/login.html
        return "Auth/login";
    }

    // Opcional: otras rutas como creación de usuarios, etc.
}
