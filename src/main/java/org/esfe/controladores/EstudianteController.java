package org.esfe.controladores;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class EstudianteController {

    @GetMapping("/estudiante/inicio")
    public String landingPage(Model model, @AuthenticationPrincipal OAuth2User principal) {
        //Verificar si hay un usuario autenticado mediante oAuth2
        if (principal != null) {
            // Agregar al modelo el nombre del estududiante autenticado a la LandingPage
            model.addAttribute("nombre", principal.getAttribute("name"));
            // Agregar el correo del usuario
            model.addAttribute("email", principal.getAttribute("email"));
            // Agregar la foto de perfil de Google
            model.addAttribute("foto", principal.getAttribute("picture"));
        }
        // Mostrar modelos en la vista
        model.addAttribute("titulo", "Bienvenido a LapSync");
        model.addAttribute("mensaje", "Aquí puedes ver tus préstamos, sanciones y requisitos.");

        return "Estudiante/inicio";
    }

}
