package org.esfe;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

public class AdministradorValidacionTest {

    // Patrones de validación (copiados del controlador)
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$");

    @Test
    void testValidacionNombresValidos() {
        // Nombres válidos
        assertTrue(NAME_PATTERN.matcher("Juan Pérez").matches());
        assertTrue(NAME_PATTERN.matcher("María José García").matches());
        assertTrue(NAME_PATTERN.matcher("José María").matches());
        assertTrue(NAME_PATTERN.matcher("Ana").matches());
        assertTrue(NAME_PATTERN.matcher("José Luis Rodríguez").matches());
        assertTrue(NAME_PATTERN.matcher("María Fernández").matches());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Juan123", "María@García", "José_Luis", "Ana-María", "Pedro.", "123", ""})
    void testValidacionNombresInvalidos(String nombreInvalido) {
        assertFalse(NAME_PATTERN.matcher(nombreInvalido).matches());
    }

    @Test
    void testValidacionEmailsValidos() {
        // Emails válidos
        assertTrue(EMAIL_PATTERN.matcher("admin@ejemplo.com").matches());
        assertTrue(EMAIL_PATTERN.matcher("usuario.admin@empresa.org").matches());
        assertTrue(EMAIL_PATTERN.matcher("test+admin@dominio.edu").matches());
        assertTrue(EMAIL_PATTERN.matcher("admin123@test.co.uk").matches());
    }

    @ParameterizedTest
    @ValueSource(strings = {"admin", "admin@", "@ejemplo.com", "admin.ejemplo.com", "admin@@ejemplo.com", ""})
    void testValidacionEmailsInvalidos(String emailInvalido) {
        assertFalse(EMAIL_PATTERN.matcher(emailInvalido).matches());
    }

    @Test
    void testValidacionPasswordsValidas() {
        // Contraseñas válidas (mínimo 8 caracteres, mayúscula, minúscula, número, carácter especial)
        assertTrue(PASSWORD_PATTERN.matcher("Password123!").matches());
        assertTrue(PASSWORD_PATTERN.matcher("Admin2024@").matches());
        assertTrue(PASSWORD_PATTERN.matcher("Secure$Pass1").matches());
        assertTrue(PASSWORD_PATTERN.matcher("MyP@ssw0rd").matches());
        assertTrue(PASSWORD_PATTERN.matcher("Test123&Valid").matches());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "password", // Sin mayúscula, número ni carácter especial
            "PASSWORD123!", // Sin minúscula
            "Password!", // Sin número
            "Password123", // Sin carácter especial
            "Pass1!", // Muy corta (menos de 8 caracteres)
            "12345678", // Solo números
            "ABCDEFGH", // Solo mayúsculas
            "abcdefgh", // Solo minúsculas
            "!@#$%^&*", // Solo caracteres especiales
            "" // Vacía
    })
    void testValidacionPasswordsInvalidas(String passwordInvalida) {
        assertFalse(PASSWORD_PATTERN.matcher(passwordInvalida).matches());
    }

    @Test
    void testValidacionCamposVacios() {
        // Campos vacíos deben ser inválidos
        assertFalse(NAME_PATTERN.matcher("").matches());
        assertFalse(NAME_PATTERN.matcher("   ").matches()); // Solo espacios
        assertFalse(EMAIL_PATTERN.matcher("").matches());
        assertFalse(PASSWORD_PATTERN.matcher("").matches());
    }

    @Test
    void testValidacionCaracteresEspecialesEnNombres() {
        // Caracteres especiales del español deben ser válidos
        assertTrue(NAME_PATTERN.matcher("José María").matches());
        assertTrue(NAME_PATTERN.matcher("Ángel").matches());
        assertTrue(NAME_PATTERN.matcher("Iñigo").matches());
        assertTrue(NAME_PATTERN.matcher("Núñez").matches());
        assertTrue(NAME_PATTERN.matcher("Óscar").matches());
        assertTrue(NAME_PATTERN.matcher("Úrsula").matches());
    }

    @Test
    void testValidacionLongitudMaxima() {
        // Nombres muy largos (aunque válidos en patrón, pueden exceder límites de BD)
        String nombreMuyLargo = "Juan Carlos María José Antonio Francisco".repeat(10);
        assertTrue(NAME_PATTERN.matcher(nombreMuyLargo).matches()); // Patrón válido

        // Email muy largo
        String emailMuyLargo = "usuario.con.nombre.muy.largo.que.excede.limites@dominio.con.nombre.muy.largo.com";
        assertTrue(EMAIL_PATTERN.matcher(emailMuyLargo).matches()); // Patrón válido
    }

    @Test
    void testCasosLimitePassword() {
        // Exactamente 8 caracteres con todos los requisitos
        assertTrue(PASSWORD_PATTERN.matcher("Abc123!@").matches());

        // 7 caracteres (debe fallar)
        assertFalse(PASSWORD_PATTERN.matcher("Abc12!@").matches());

        // Con espacios (no permitidos)
        assertFalse(PASSWORD_PATTERN.matcher("Pass 123!").matches());
    }
}
