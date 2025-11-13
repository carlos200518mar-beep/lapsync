package org.esfe;

import org.esfe.controladores.AdministradorController;
import org.esfe.modelos.User;
import org.esfe.repositorios.IUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdministradorControllerTest {

    @Mock
    private IUserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private AdministradorController administradorController;

    private User administrador;

    @BeforeEach
    void setUp() {
        administrador = new User();
        administrador.setId(1);
        administrador.setFullName("Juan Pérez");
        administrador.setEmail("juan.perez@admin.com");
        administrador.setRole("admin");
    }

    @Test
    @SuppressWarnings("null")
    void testListarAdministradores() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(administrador));
        String viewName = administradorController.listarAdministradores(model);
        assertEquals("Administrador/listar", viewName);
        verify(model).addAttribute(eq("administradores"), anyList());
    }

    @Test
    @SuppressWarnings("null")
    void testMostrarFormularioCrear() {
        String viewName = administradorController.mostrarFormularioCrear(model);
        assertEquals("Administrador/crear", viewName);
        verify(model).addAttribute(eq("administrador"), any(User.class));
    }

    @Test
    @SuppressWarnings("null")
    void testCrearAdministradorExitoso() {
        User nuevoAdmin = new User();
        nuevoAdmin.setFullName("María García");
        nuevoAdmin.setEmail("maria.garcia@admin.com");
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        String viewName = administradorController.crearAdministrador(nuevoAdmin, bindingResult, model, redirectAttributes, "Password123!", "Password123!");
        assertEquals("redirect:/administrador/listar", viewName);
        verify(userRepository).save(any(User.class));
        verify(redirectAttributes).addFlashAttribute("mensaje", "Administrador registrado exitosamente");
    }

    @Test
    @SuppressWarnings("null")
    void testCrearAdministradorConPasswordsNoCoincidentes() {
        // Arrange
        User nuevoAdmin = new User();
        nuevoAdmin.setFullName("María García");
        nuevoAdmin.setEmail("maria.garcia@admin.com");
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act
        String viewName = administradorController.crearAdministrador(nuevoAdmin, bindingResult, model, redirectAttributes, "Password123!", "DifferentPassword!");

        // Assert
        assertEquals("Administrador/crear", viewName);
        verify(bindingResult).rejectValue("passwordHash", "error.password", "Las contraseñas no coinciden");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testMostrarFormularioEditar() {
        when(userRepository.findById(1)).thenReturn(Optional.of(administrador));
        String viewName = administradorController.mostrarFormularioEditar(1, model, redirectAttributes);
        assertEquals("Administrador/editar", viewName);
        verify(model).addAttribute("administrador", administrador);
    }

    @Test
    void testMostrarFormularioEditarAdministradorNoExiste() {
        when(userRepository.findById(999)).thenReturn(Optional.empty());
        String viewName = administradorController.mostrarFormularioEditar(999, model, redirectAttributes);
        assertEquals("redirect:/administrador/listar", viewName);
        verify(redirectAttributes).addFlashAttribute("mensaje", "Administrador no encontrado");
    }

    @Test
    @SuppressWarnings("null")
    void testEditarAdministradorExitoso() {
        User adminEditado = new User();
        adminEditado.setFullName("Juan Carlos Pérez");
        adminEditado.setEmail("juan.perez@admin.com");
        when(userRepository.findById(1)).thenReturn(Optional.of(administrador));
        when(bindingResult.hasErrors()).thenReturn(false);
        String viewName = administradorController.editarAdministrador(1, adminEditado, bindingResult, model, redirectAttributes, "", "");
        assertEquals("redirect:/administrador/listar", viewName);
        verify(userRepository).save(any(User.class));
        verify(redirectAttributes).addFlashAttribute("mensaje", "Administrador actualizado exitosamente");
    }

    @Test
    void testEliminarAdministradorExitoso() {
        when(userRepository.existsById(1)).thenReturn(true);
        String viewName = administradorController.eliminarAdministrador(1, redirectAttributes);
        assertEquals("redirect:/administrador/listar", viewName);
        verify(userRepository).deleteById(1);
        verify(redirectAttributes).addFlashAttribute("mensaje", "Administrador eliminado exitosamente");
    }
}