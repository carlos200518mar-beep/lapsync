package org.esfe.controladores;

import jakarta.validation.Valid;
import org.esfe.modelos.Loans;
import org.esfe.modelos.User;
import org.esfe.repositorios.ILoansRepository;
import org.esfe.repositorios.IPenaltyRepository;
import org.esfe.repositorios.IUserRepository;
import org.esfe.repositorios.LaptopRepository;
import org.esfe.servicios.interfaces.IAdminService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/administrador")
public class AdministradorController {

    private final IUserRepository userRepository;
    private final LaptopRepository laptopRepository;
    private final ILoansRepository loansRepository;
    private final IPenaltyRepository penaltyRepository;
    private final IAdminService adminService;

    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");
    private static final Pattern NAME_PATTERN     = Pattern.compile("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$");

    public AdministradorController(IUserRepository userRepository,
                                   LaptopRepository laptopRepository,
                                   ILoansRepository loansRepository,
                                   IPenaltyRepository penaltyRepository,
                                   IAdminService adminService) {
        this.userRepository   = userRepository;
        this.laptopRepository = laptopRepository;
        this.loansRepository  = loansRepository;
        this.penaltyRepository= penaltyRepository;
        this.adminService     = adminService;
    }

    private boolean isSuperAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_SUPERADMIN".equals(a.getAuthority()));
    }

    private long countSuperAdmins() {
        return userRepository.findAll().stream()
                .filter(u -> "superadmin".equalsIgnoreCase(u.getRole()))
                .count();
    }

    // =======================
    // DASHBOARD
    // =======================
    @GetMapping("/dashboard")
    public String mostrarDashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userName = (auth != null) ? auth.getName() : "Usuario";
        String userRole = "USER";
        if (auth != null && !auth.getAuthorities().isEmpty()) {
            userRole = auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        }
        model.addAttribute("userName", userName);
        model.addAttribute("userRole", userRole);

        long laptopsTotales     = laptopRepository.count();
        long laptopsDisponibles = laptopRepository.countByStatus("available");
        long prestamosActivos   = loansRepository.countByStatus("active");
        long sancionesActivas   = penaltyRepository.countByIsResolvedFalse();

        LocalDateTime now   = LocalDateTime.now().withNano(0);
        LocalDateTime limit = now.plusMinutes(25);
        long porVencer      = loansRepository.countDueBetween(now, limit);

        List<Loans> activos = loansRepository
                .findByStatusAndDeliveredAtIsNotNullAndReturnedAtIsNull("active");
        List<Loans> porVencerLista = activos.stream()
                .filter(l -> l.getRequestedHours() != null && l.getDeliveredAt() != null)
                .map(l -> new Object[]{l, l.getDeliveredAt().plusHours(l.getRequestedHours())})
                .filter(arr -> {
                    LocalDateTime due = (LocalDateTime) arr[1];
                    return (!due.isBefore(now)) && (!due.isAfter(limit));
                })
                .sorted(Comparator.comparing(arr -> (LocalDateTime) arr[1]))
                .limit(5)
                .map(arr -> (Loans) arr[0])
                .toList();

        var pendientes = loansRepository
                .findByStatusOrderByRequestedAtDesc("pending", PageRequest.of(0, 5))
                .getContent();

        var reparacion = laptopRepository.findRepairOrdered("repair", PageRequest.of(0, 5));

        model.addAttribute("laptopsTotales", laptopsTotales);
        model.addAttribute("laptopsDisponibles", laptopsDisponibles);
        model.addAttribute("prestamosActivos", prestamosActivos);
        model.addAttribute("porVencer", porVencer);
        model.addAttribute("porVencerLista", porVencerLista);
        model.addAttribute("sancionesActivas", sancionesActivas);
        model.addAttribute("solicitudesRecientes", pendientes);
        model.addAttribute("laptopsEnReparacion", reparacion);

        return "dashboard/index";
    }

    // =======================
    // CRUD ADMINISTRADORES
    // =======================
    @GetMapping("/listar")
    public String listarAdministradores(Model model) {
        List<User> todosUsuarios        = userRepository.findAll();
        List<User> administradores      = todosUsuarios.stream()
                .filter(u -> "admin".equalsIgnoreCase(u.getRole()))
                .collect(Collectors.toList());
        List<User> superAdministradores = todosUsuarios.stream()
                .filter(u -> "superadmin".equalsIgnoreCase(u.getRole()))
                .collect(Collectors.toList());

        model.addAttribute("administradores", administradores);
        model.addAttribute("superAdministradores", superAdministradores);
        model.addAttribute("totalAdministradores", administradores.size() + superAdministradores.size());
        model.addAttribute("titulo", "Gestión de Administradores");

        // Flag: solo un SUPERADMIN logueado puede editar superadmins
        model.addAttribute("canEditSuper", isSuperAdmin());

        return "Administrador/listar";
    }

    @GetMapping("/crear")
    public String mostrarFormularioCrear(Model model) {
        User nuevo = new User();
        nuevo.setRole("admin");
        model.addAttribute("administrador", nuevo);
        model.addAttribute("titulo", "Registrar Nuevo Administrador / Superadmin");
        model.addAttribute("canCreateSuper", isSuperAdmin());
        return "Administrador/crear";
    }

    @PostMapping("/crear")
    public String crearAdministrador(@Valid @ModelAttribute("administrador") User administrador,
                                     BindingResult result,
                                     Model model,
                                     RedirectAttributes ra,
                                     @RequestParam("password") String password,
                                     @RequestParam("confirmPassword") String confirmPassword) {

        if (result.hasErrors()) {
            model.addAttribute("titulo", "Registrar Nuevo Administrador / Superadmin");
            model.addAttribute("canCreateSuper", isSuperAdmin());
            return "Administrador/crear";
        }
        if (!NAME_PATTERN.matcher(administrador.getFullName()).matches()) {
            result.rejectValue("fullName", "error.fullName", "El nombre solo debe contener letras y espacios");
            model.addAttribute("titulo", "Registrar Nuevo Administrador / Superadmin");
            model.addAttribute("canCreateSuper", isSuperAdmin());
            return "Administrador/crear";
        }
        if (userRepository.findByEmail(administrador.getEmail()).isPresent()) {
            result.rejectValue("email", "error.email", "Ya existe un usuario con este correo electrónico");
            model.addAttribute("titulo", "Registrar Nuevo Administrador / Superadmin");
            model.addAttribute("canCreateSuper", isSuperAdmin());
            return "Administrador/crear";
        }
        if (!password.equals(confirmPassword)) {
            result.rejectValue("passwordHash", "error.password", "Las contraseñas no coinciden");
            model.addAttribute("titulo", "Registrar Nuevo Administrador / Superadmin");
            model.addAttribute("canCreateSuper", isSuperAdmin());
            return "Administrador/crear";
        }
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            result.rejectValue("passwordHash", "error.password", "La contraseña no cumple los requisitos de seguridad");
            model.addAttribute("titulo", "Registrar Nuevo Administrador / Superadmin");
            model.addAttribute("canCreateSuper", isSuperAdmin());
            return "Administrador/crear";
        }

        String desiredRole = Optional.ofNullable(administrador.getRole())
                .map(String::toLowerCase).orElse("admin");
        Set<String> allowed = Set.of("admin", "superadmin");
        if (!allowed.contains(desiredRole)) desiredRole = "admin";

        if ("superadmin".equals(desiredRole) && !isSuperAdmin()) {
            result.rejectValue("role", "error.role", "No tienes permisos para crear SUPERADMIN.");
            model.addAttribute("titulo", "Registrar Nuevo Administrador / Superadmin");
            model.addAttribute("canCreateSuper", isSuperAdmin());
            return "Administrador/crear";
        }

        administrador.setRole(desiredRole);
        adminService.crearAdmin(administrador, password);

        ra.addFlashAttribute("mensaje", "Usuario (" + administrador.getRole() + ") registrado exitosamente");
        ra.addFlashAttribute("tipoMensaje", "success");
        return "redirect:/administrador/listar";
    }

    @GetMapping("/editar/{id}")
    @SuppressWarnings("null")
    public String mostrarFormularioEditar(@PathVariable Integer id, Model model, RedirectAttributes ra) {
        Optional<User> adminOpt = userRepository.findById(id);
        if (adminOpt.isPresent()) {
            model.addAttribute("administrador", adminOpt.get());
            model.addAttribute("titulo", "Editar Administrador / Superadmin");
            model.addAttribute("canEditRole", isSuperAdmin());
            return "Administrador/editar";
        }
        ra.addFlashAttribute("mensaje", "Administrador no encontrado");
        ra.addFlashAttribute("tipoMensaje", "error");
        return "redirect:/administrador/listar";
    }

    @PostMapping("/editar/{id}")
    @SuppressWarnings("null")
    public String editarAdministrador(@PathVariable Integer id,
                                      @Valid @ModelAttribute("administrador") User administrador,
                                      BindingResult result,
                                      Model model,
                                      RedirectAttributes ra,
                                      @RequestParam(value = "password", required = false) String password,
                                      @RequestParam(value = "confirmPassword", required = false) String confirmPassword) {

        Optional<User> adminExistenteOpt = userRepository.findById(id);
        if (adminExistenteOpt.isEmpty()) {
            ra.addFlashAttribute("mensaje", "Administrador no encontrado");
            ra.addFlashAttribute("tipoMensaje", "error");
            return "redirect:/administrador/listar";
        }

        userRepository.findByEmail(administrador.getEmail()).ifPresent(u -> {
            if (!u.getId().equals(id)) {
                result.rejectValue("email", "error.email", "Ya existe otro usuario con este correo electrónico");
            }
        });

        boolean passwordProvided = password != null && !password.trim().isEmpty();
        if (passwordProvided) {
            assert password != null; // Garantizado por passwordProvided
            if (!password.equals(confirmPassword)) {
                result.rejectValue("passwordHash", "error.password", "Las contraseñas no coinciden");
            }
            if (!PASSWORD_PATTERN.matcher(password).matches()) {
                result.rejectValue("passwordHash", "error.password", "La nueva contraseña no cumple los requisitos de seguridad");
            }
        }

        User adminExistente = adminExistenteOpt.get();
        String currentRole  = Optional.ofNullable(adminExistente.getRole()).orElse("admin").toLowerCase();
        String desiredRole  = Optional.ofNullable(administrador.getRole()).orElse(currentRole).toLowerCase();

        Set<String> allowed = Set.of("admin", "superadmin");
        if (!allowed.contains(desiredRole)) desiredRole = currentRole;

        boolean roleChanged = !desiredRole.equalsIgnoreCase(currentRole);

        if (roleChanged && !isSuperAdmin()) {
            result.rejectValue("role", "error.role", "No tienes permisos para cambiar el rol.");
        }

        if (roleChanged && "superadmin".equals(currentRole) && "admin".equals(desiredRole)) {
            long supers = countSuperAdmins();
            if (supers <= 1) {
                result.rejectValue("role", "error.role", "No puedes dejar el sistema sin SUPERADMIN.");
            }
        }

        if (roleChanged && "superadmin".equals(currentRole) && !isSuperAdmin()) {
            result.rejectValue("role", "error.role", "No puedes modificar el rol de un SUPERADMIN.");
        }

        if (result.hasErrors()) {
            model.addAttribute("titulo", "Editar Administrador / Superadmin");
            model.addAttribute("canEditRole", isSuperAdmin());
            administrador.setId(id);
            return "Administrador/editar";
        }

        adminExistente.setFullName(administrador.getFullName());
        adminExistente.setEmail(administrador.getEmail());
        if (roleChanged) adminExistente.setRole(desiredRole);

        if (passwordProvided) {
            adminService.actualizarPassword(adminExistente, password);
        } else {
            userRepository.save(adminExistente);
        }

        ra.addFlashAttribute("mensaje", "Usuario actualizado correctamente");
        ra.addFlashAttribute("tipoMensaje", "success");
        return "redirect:/administrador/listar";
    }

    @PostMapping("/eliminar/{id}")
    @SuppressWarnings("null")
    public String eliminarAdministrador(@PathVariable Integer id, RedirectAttributes ra) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            ra.addFlashAttribute("mensaje", "Administrador no encontrado");
            ra.addFlashAttribute("tipoMensaje", "error");
            return "redirect:/administrador/listar";
        }

        User target = userOpt.get();

        if ("superadmin".equalsIgnoreCase(target.getRole())) {
            ra.addFlashAttribute("mensaje", "No puedes eliminar a un SUPERADMIN.");
            ra.addFlashAttribute("tipoMensaje", "error");
            return "redirect:/administrador/listar";
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            userRepository.findByEmail(auth.getName()).ifPresent(current -> {
                if (current.getId().equals(id)) {
                    throw new IllegalArgumentException("No puedes eliminar tu propia cuenta.");
                }
            });
        }

        try {
            userRepository.delete(target);
            ra.addFlashAttribute("mensaje", "Administrador eliminado exitosamente");
            ra.addFlashAttribute("tipoMensaje", "success");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("mensaje", e.getMessage());
            ra.addFlashAttribute("tipoMensaje", "error");
        } catch (DataIntegrityViolationException dive) {
            ra.addFlashAttribute("mensaje", "No se puede eliminar: el usuario tiene registros asociados.");
            ra.addFlashAttribute("tipoMensaje", "error");
        } catch (Exception e) {
            ra.addFlashAttribute("mensaje", "Error al eliminar el administrador.");
            ra.addFlashAttribute("tipoMensaje", "error");
        }
        return "redirect:/administrador/listar";
    }

    // Stubs exportación
    @GetMapping("/exportar") public ResponseEntity<String> exportarAdministradores() { return null; }
    @GetMapping("/exportar-pdf") public ResponseEntity<byte[]> exportarAdministradoresPDF() { return null; }
}
