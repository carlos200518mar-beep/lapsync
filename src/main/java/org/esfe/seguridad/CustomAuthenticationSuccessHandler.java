package org.esfe.seguridad;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final RedirectStrategy redirect = new DefaultRedirectStrategy();
    private final RequestCache requestCache = new HttpSessionRequestCache();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
        
        System.out.println("🟣 CustomAuthenticationSuccessHandler - Roles detectados: " + roles);

        // Determinar URL de redirección según el rol
        String targetUrl;
        
        if (roles.contains("ROLE_SUPERADMIN") || roles.contains("ROLE_ADMIN")) {
            targetUrl = "/administrador/dashboard";
            System.out.println("🟣 Redirigiendo a: " + targetUrl + " (ADMIN/SUPERADMIN)");
        } else if (roles.contains("ROLE_STUDENT")) {
            targetUrl = "/estudiante/inicio";
            System.out.println("🟣 Redirigiendo a: " + targetUrl + " (STUDENT)");
        } else {
            targetUrl = "/";
            System.out.println("🟣 Redirigiendo a: " + targetUrl + " (FALLBACK - Sin rol reconocido)");
        }

        // Limpiar la caché de requests guardados
        requestCache.removeRequest(request, response);
        
        // Realizar la redirección
        redirect.sendRedirect(request, response, targetUrl);
    }
}
