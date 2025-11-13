package org.esfe.seguridad;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.SavedRequest;
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

        // 1) Si hay una SavedRequest (el user intentó entrar a una URL protegida), respétala
        SavedRequest saved = requestCache.getRequest(request, response);
        if (saved != null) {
            String redirectUrl = saved.getRedirectUrl();

            // Evitar mandar a un estudiante a /administrador/**
            boolean isAdminArea = redirectUrl.contains("/administrador/");
            boolean isAdmin = roles.contains("ROLE_ADMIN") || roles.contains("ROLE_SUPERADMIN");

            if (isAdminArea && !isAdmin) {
                redirect.sendRedirect(request, response, "/estudiante/inicio");
                return;
            }
            redirect.sendRedirect(request, response, redirectUrl);
            return;
        }

        // 2) Redirección por rol (cuando no hay SavedRequest)
        if (roles.contains("ROLE_SUPERADMIN") || roles.contains("ROLE_ADMIN")) {
            redirect.sendRedirect(request, response, "/administrador/dashboard");
        } else if (roles.contains("ROLE_STUDENT")) {
            redirect.sendRedirect(request, response, "/estudiante/inicio");
        } else {
            // fallback
            redirect.sendRedirect(request, response, "/");
        }
    }
}
