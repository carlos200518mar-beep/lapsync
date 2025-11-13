package org.esfe.config;

import org.esfe.seguridad.CustomAuthenticationSuccessHandler;
import org.esfe.seguridad.CustomOAuth2AuthenticationFailureHandler;
import org.esfe.seguridad.CustomOAuth2UserService;
import org.esfe.seguridad.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomAuthenticationSuccessHandler successHandler;
    private final CustomOAuth2AuthenticationFailureHandler failureHandler;
    private final PasswordEncoder passwordEncoder;

    public SecurityConfig(
            CustomUserDetailsService userDetailsService,
            CustomOAuth2UserService customOAuth2UserService,
            CustomAuthenticationSuccessHandler successHandler,
            CustomOAuth2AuthenticationFailureHandler failureHandler,
            PasswordEncoder passwordEncoder
    ) {
        this.userDetailsService = userDetailsService;
        this.customOAuth2UserService = customOAuth2UserService;
        this.successHandler = successHandler;
        this.failureHandler = failureHandler;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    @SuppressWarnings("deprecation")
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/oauth2/**", "/css/**", "/js/**", "/images/**", "/error").permitAll()
                        .requestMatchers("/administrador/**").hasAnyRole("ADMIN", "SUPERADMIN")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/superadmin/**").hasRole("SUPERADMIN")
                        .requestMatchers("/estudiante/**", "/sanciones/mis-sanciones").hasRole("STUDENT")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login/local")     // tu formulario POST -> /login/local
                        .successHandler(successHandler)
                        .failureUrl("/login?error=credenciales") // 👈 mensaje "credenciales incorrectas"
                        .permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .userInfoEndpoint(u -> u.userService(customOAuth2UserService))
                        .successHandler(successHandler)
                        .failureHandler(failureHandler)          // 👈 redirige a /login?error=dominio si el dominio no es válido
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                .build();
    }
}
