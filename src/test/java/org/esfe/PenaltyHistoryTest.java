package org.esfe;

import org.esfe.modelos.Penalty;
import org.esfe.modelos.User;
import org.esfe.repositorios.IUserRepository;
import org.esfe.servicios.interfaces.IPenaltyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PenaltyHistoryTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IPenaltyService penaltyService;

    @Autowired
    private IUserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Crear usuario de prueba
        testUser = new User();
        testUser.setFullName("Juan Pérez Test");
        testUser.setEmail("juan.test@example.com");
        testUser.setStudentId("TEST001");
        testUser.setCareer("Ingeniería en Sistemas");
        testUser.setRole("student");
        testUser.setIsActive(true);
        testUser = userRepository.save(testUser);
    }

    @Test
    void testHistorialSancionesCompleto() {
        // Crear múltiples sanciones con diferentes estados y tipos
        Penalty sancionActiva1 = crearSancion("Daño físico", "Rayón en pantalla de laptop", new BigDecimal("50.00"), false);
        Penalty sancionActiva2 = crearSancion("Exceso de tiempo", "Entrega tardía de 2 horas", BigDecimal.ZERO, false);
        Penalty sancionResuelta = crearSancion("Sacar equipo fuera de institución", "Laptop encontrada fuera del campus", new BigDecimal("100.00"), true);

        penaltyService.guardar(sancionActiva1);
        penaltyService.guardar(sancionActiva2);
        penaltyService.guardar(sancionResuelta);

        // Verificar historial completo
        List<Penalty> historialCompleto = penaltyService.buscarPorUsuarioId(testUser.getId());
        assertEquals(3, historialCompleto.size());

        // Verificar sanciones activas
        List<Penalty> sancionesActivas = penaltyService.buscarActivasPorUsuario(testUser.getId());
        assertEquals(2, sancionesActivas.size());

        // Verificar conteo de sanciones activas
        Long conteoActivas = penaltyService.contarSancionesActivas(testUser.getId());
        assertEquals(2L, conteoActivas);

        // Verificar que el usuario tiene sanciones activas
        assertTrue(penaltyService.tienesSancionesActivas(testUser.getId()));
    }

    @Test
    void testTiposDeSanciones() {
        // Crear una sanción de cada tipo válido
        Penalty danioFisico = penaltyService.crearSancionDanioFisico(testUser, "Pantalla rota", 75.0);
        Penalty excesoTiempo = penaltyService.crearSancionExcesoTiempo(testUser, "3 horas de retraso");
        Penalty sacarEquipo = penaltyService.crearSancionSacarEquipo(testUser, "Laptop en casa", 150.0);

        // Verificar que se crearon correctamente
        assertNotNull(danioFisico.getId());
        assertEquals("Daño físico", danioFisico.getType());
        assertEquals(new BigDecimal("75.0"), danioFisico.getFineAmount());

        assertNotNull(excesoTiempo.getId());
        assertEquals("Exceso de tiempo", excesoTiempo.getType());
        assertEquals(BigDecimal.ZERO, excesoTiempo.getFineAmount());

        assertNotNull(sacarEquipo.getId());
        assertEquals("Sacar equipo fuera de institución", sacarEquipo.getType());
        assertEquals(new BigDecimal("150.0"), sacarEquipo.getFineAmount());

        // Verificar filtrado por tipo
        List<Penalty> sanciones = penaltyService.buscarPorTipo("Daño físico");
        assertEquals(1, sanciones.size());
        assertEquals(danioFisico.getId(), sanciones.get(0).getId());
    }

    @Test
    void testResolucionDeSanciones() {
        // Crear sanción activa
        Penalty sancion = crearSancion("Daño físico", "Test resolución", new BigDecimal("25.00"), false);
        Penalty sancionGuardada = penaltyService.guardar(sancion);

        // Verificar que está activa
        assertFalse(sancionGuardada.getIsResolved());
        assertNull(sancionGuardada.getResolvedAt());

        // Resolver la sanción
        Penalty sancionResuelta = penaltyService.resolverSancion(sancionGuardada.getId());

        // Verificar que se resolvió correctamente
        assertTrue(sancionResuelta.getIsResolved());
        assertNotNull(sancionResuelta.getResolvedAt());

        // Verificar que ya no aparece en sanciones activas
        List<Penalty> sancionesActivas = penaltyService.buscarActivasPorUsuario(testUser.getId());
        assertEquals(0, sancionesActivas.size());

        // Verificar que aparece en sanciones resueltas
        List<Penalty> sancionesResueltas = penaltyService.buscarResueltas();
        assertTrue(sancionesResueltas.stream().anyMatch(p -> p.getId().equals(sancionResuelta.getId())));
    }

    @Test
    void testHistorialVacio() {
        // Verificar comportamiento con usuario sin sanciones
        List<Penalty> historialVacio = penaltyService.buscarPorUsuarioId(testUser.getId());
        assertTrue(historialVacio.isEmpty());

        List<Penalty> sancionesActivas = penaltyService.buscarActivasPorUsuario(testUser.getId());
        assertTrue(sancionesActivas.isEmpty());

        Long conteo = penaltyService.contarSancionesActivas(testUser.getId());
        assertEquals(0L, conteo);

        assertFalse(penaltyService.tienesSancionesActivas(testUser.getId()));
    }

    @Test
    @SuppressWarnings("null")
    void testVistaHistorialSanciones() throws Exception {
        // Crear algunas sanciones para mostrar en la vista
        penaltyService.crearSancionDanioFisico(testUser, "Sanción de prueba 1", 30.0);
        penaltyService.crearSancionExcesoTiempo(testUser, "Sanción de prueba 2");

        // Crear OAuth2User mock con los atributos necesarios
        OAuth2User oauth2User = new DefaultOAuth2User(
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")),
                Map.of(
                        "email", testUser.getEmail(),
                        "name", testUser.getFullName()
                ),
                "email"
        );

        mockMvc.perform(get("/sanciones/mis-sanciones")
                        .with(SecurityMockMvcRequestPostProcessors.oauth2Login().oauth2User(oauth2User)))
                .andExpect(status().isOk())
                .andExpect(view().name("sanciones/mis-sanciones"))
                .andExpect(model().attributeExists("sanciones"))
                .andExpect(model().attributeExists("sancionesActivas"))
                .andExpect(model().attributeExists("usuario"))
                .andExpect(model().attribute("titulo", "Mi Historial de Sanciones"));
    }

    @Test
    void testFiltradoPorEstado() {
        // Crear sanciones con diferentes estados
        penaltyService.crearSancionDanioFisico(testUser, "Activa 1", 20.0);
        penaltyService.crearSancionExcesoTiempo(testUser, "Activa 2");
        Penalty resuelta = penaltyService.crearSancionSacarEquipo(testUser, "Resuelta", 50.0);

        // Resolver una sanción
        penaltyService.resolverSancion(resuelta.getId());

        // Verificar filtrado por estado activo
        List<Penalty> activas = penaltyService.buscarActivas();
        assertEquals(2, activas.size());
        assertTrue(activas.stream().allMatch(p -> !p.getIsResolved()));

        // Verificar filtrado por estado resuelto
        List<Penalty> resueltas = penaltyService.buscarResueltas();
        assertEquals(1, resueltas.size());
        assertTrue(resueltas.stream().allMatch(p -> p.getIsResolved()));
    }

    @Test
    void testValidacionesDeSancion() {
        // Test de validaciones del modelo Penalty
        Penalty sancion = new Penalty();
        sancion.setUser(testUser);
        sancion.setType("Daño físico");
        sancion.setDescription("Descripción válida");
        sancion.setFineAmount(new BigDecimal("50.00"));
        sancion.setIsResolved(false);

        // Guardar sanción válida
        Penalty sancionGuardada = penaltyService.guardar(sancion);
        assertNotNull(sancionGuardada.getId());
        assertNotNull(sancionGuardada.getCreatedAt());

        // Verificar timestamps automáticos
        assertTrue(sancionGuardada.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
        assertTrue(sancionGuardada.getCreatedAt().isAfter(LocalDateTime.now().minusMinutes(1)));
    }

    @Test
    void testBusquedaPorUsuarioInexistente() {
        // Buscar sanciones de usuario que no existe
        List<Penalty> sanciones = penaltyService.buscarPorUsuarioId(999);
        assertTrue(sanciones.isEmpty());

        Long conteo = penaltyService.contarSancionesActivas(999);
        assertEquals(0L, conteo);

        assertFalse(penaltyService.tienesSancionesActivas(999));
    }

    private Penalty crearSancion(String tipo, String descripcion, BigDecimal multa, boolean resuelta) {
        Penalty penalty = new Penalty();
        penalty.setUser(testUser);
        penalty.setType(tipo);
        penalty.setDescription(descripcion);
        penalty.setFineAmount(multa);
        penalty.setIsResolved(resuelta);

        if (resuelta) {
            penalty.setResolvedAt(LocalDateTime.now().minusDays(1));
        }

        return penalty;
    }
}
