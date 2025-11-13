package org.esfe.seguridad;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomOAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        String target = "/login?error";

        if (exception instanceof OAuth2AuthenticationException oae) {
            String code = (oae.getError() != null) ? oae.getError().getErrorCode() : null;

            if ("invalid_domain".equalsIgnoreCase(code)) {
                target = "/login?error=dominio";     // 👈 mostrará el mensaje de dominio
            } else {
                // compatibilidad con tu versión anterior que miraba el texto
                String msg = oae.getMessage();
                if (msg != null && msg.contains("@esfe.agape.edu.sv")) {
                    target = "/login?error=dominio";
                }
            }
        }

        getRedirectStrategy().sendRedirect(request, response, target);
    }
}
