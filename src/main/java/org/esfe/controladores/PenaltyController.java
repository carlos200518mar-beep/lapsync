package org.esfe.controladores;

import org.esfe.modelos.Penalty;
import org.esfe.modelos.User;
import org.esfe.repositorios.IUserRepository;
import org.esfe.servicios.interfaces.IPenaltyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.math.BigDecimal; // <-- Esta es la línea que soluciona el problema

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.io.ByteArrayOutputStream;


@Controller
@RequestMapping("/sanciones")
public class PenaltyController {

    @Autowired
    private IPenaltyService penaltyService;

    @Autowired
    private IUserRepository userRepository;

    // Vista para estudiantes - Ver sus propias sanciones
    @GetMapping("/mis-sanciones")
    public String misSanciones(Model model, @AuthenticationPrincipal OAuth2User principal) {
        if (principal != null) {
            String email = principal.getAttribute("email");
            Optional<User> userOpt = userRepository.findByEmail(email);

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                List<Penalty> sanciones = penaltyService.buscarPorUsuarioId(user.getId());
                List<Penalty> sancionesActivas = penaltyService.buscarActivasPorUsuario(user.getId());

                model.addAttribute("sanciones", sanciones);
                model.addAttribute("sancionesActivas", sancionesActivas);
                model.addAttribute("usuario", user);
                model.addAttribute("titulo", "Mi Historial de Sanciones");
                // Agregar foto de perfil de Google
                model.addAttribute("nombre", principal.getAttribute("name"));
                model.addAttribute("email", principal.getAttribute("email"));
                model.addAttribute("foto", principal.getAttribute("picture"));
            }
        }
        return "sanciones/mis-sanciones";
    }

    // Vista para administradores - Ver todas las sanciones
    @GetMapping("/admin/listar")
    public String listarTodasSanciones(Model model) {
        List<Penalty> todasSanciones = penaltyService.listarTodas();
        List<Penalty> sancionesActivas = penaltyService.buscarActivas();
        List<Penalty> sancionesResueltas = penaltyService.buscarResueltas();

        long estudiantesBloqueados = sancionesActivas.stream()
                .map(sancion -> sancion.getUser().getId())
                .distinct()
                .count();

        model.addAttribute("todasSanciones", todasSanciones);
        model.addAttribute("sancionesActivas", sancionesActivas);
        model.addAttribute("sancionesResueltas", sancionesResueltas);
        model.addAttribute("estudiantesBloqueados", estudiantesBloqueados);
        model.addAttribute("titulo", "Gestión de Sanciones");

        return "sanciones/admin-listar";
    }

    // Formulario para crear nueva sanción (solo admin)
    @GetMapping("/admin/crear")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("penalty", new Penalty());
        model.addAttribute("usuarios", userRepository.findAll());
        model.addAttribute("titulo", "Crear Nueva Sanción");
        return "sanciones/admin-crear";
    }

    // Procesar creación de sanción
    @PostMapping("/admin/crear")
    public String crearSancion(@Valid @ModelAttribute Penalty penalty,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("usuarios", userRepository.findAll());
            model.addAttribute("titulo", "Crear Nueva Sanción");
            return "sanciones/admin-crear";
        }

        try {
            penaltyService.guardar(penalty);
            redirectAttributes.addFlashAttribute("mensaje", "Sanción creada exitosamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al crear la sanción: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
        }

        return "redirect:/sanciones/admin/listar";
    }

    // Resolver sanción (marcar como resuelta)
    @PostMapping("/admin/resolver/{id}")
    public String resolverSancion(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            penaltyService.resolverSancion(id);
            redirectAttributes.addFlashAttribute("mensaje", "Sanción resuelta exitosamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al resolver la sanción: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
        }

        return "redirect:/sanciones/admin/listar";
    }

    @PostMapping("/admin/resolver-multiples")
    public String resolverSancionesMultiples(@RequestParam("sancionIds") List<Integer> sancionIds,
                                             RedirectAttributes redirectAttributes) {
        try {
            int resueltas = 0;
            for (Integer id : sancionIds) {
                penaltyService.resolverSancion(id);
                resueltas++;
            }
            redirectAttributes.addFlashAttribute("mensaje",
                    "Se resolvieron " + resueltas + " sanciones exitosamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje",
                    "Error al resolver las sanciones: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
        }

        return "redirect:/sanciones/admin/listar";
    }

    @GetMapping("/admin/exportar")
    public ResponseEntity<String> exportarSanciones() {
        try {
            List<Penalty> sanciones = penaltyService.listarTodas();
            StringBuilder csv = new StringBuilder();

            // Encabezados
            csv.append("ID,Usuario,Email,Tipo,Descripción,Multa,Fecha,Estado\n");

            // Datos
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            for (Penalty sancion : sanciones) {
                csv.append(sancion.getId()).append(",")
                        .append("\"").append(sancion.getUser().getFullName()).append("\",")
                        .append("\"").append(sancion.getUser().getEmail()).append("\",")
                        .append("\"").append(sancion.getType()).append("\",")
                        .append("\"").append(sancion.getDescription().replace("\"", "\"\"")).append("\",")
                        .append(sancion.getFineAmount() != null ? sancion.getFineAmount() : "0").append(",")
                        .append("\"").append(sancion.getCreatedAt().format(formatter)).append("\",")
                        .append(sancion.getIsResolved() ? "Resuelta" : "Activa").append("\n");
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            headers.setContentDispositionFormData("attachment", "sanciones_" +
                    java.time.LocalDate.now().toString() + ".csv");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(csv.toString());

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al exportar: " + e.getMessage());
        }
    }

    @GetMapping("/admin/exportar-pdf")
    public ResponseEntity<byte[]> exportarSancionesPDF() {
        try {
            List<Penalty> sanciones = penaltyService.listarTodas();

            // Crear documento PDF
            Document document = new Document(PageSize.A4);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, baos);

            document.open();

            // Título del documento
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.DARK_GRAY);
            Paragraph title = new Paragraph("REPORTE DE SANCIONES", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Información del reporte
            Font infoFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.GRAY);
            Paragraph info = new Paragraph("Generado el: " +
                    java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")),
                    infoFont);
            info.setAlignment(Element.ALIGN_RIGHT);
            info.setSpacingAfter(20);
            document.add(info);

            // Estadísticas
            List<Penalty> sancionesActivas = penaltyService.buscarActivas();
            List<Penalty> sancionesResueltas = penaltyService.buscarResueltas();

            PdfPTable statsTable = new PdfPTable(3);
            statsTable.setWidthPercentage(100);
            statsTable.setSpacingAfter(20);

            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE);
            PdfPCell headerCell1 = new PdfPCell(new Phrase("Total Sanciones", headerFont));
            headerCell1.setBackgroundColor(BaseColor.DARK_GRAY);
            headerCell1.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell1.setPadding(10);

            PdfPCell headerCell2 = new PdfPCell(new Phrase("Sanciones Activas", headerFont));
            headerCell2.setBackgroundColor(BaseColor.ORANGE);
            headerCell2.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell2.setPadding(10);

            PdfPCell headerCell3 = new PdfPCell(new Phrase("Sanciones Resueltas", headerFont));
            headerCell3.setBackgroundColor(BaseColor.GREEN);
            headerCell3.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell3.setPadding(10);

            statsTable.addCell(headerCell1);
            statsTable.addCell(headerCell2);
            statsTable.addCell(headerCell3);

            Font dataFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            PdfPCell dataCell1 = new PdfPCell(new Phrase(String.valueOf(sanciones.size()), dataFont));
            dataCell1.setHorizontalAlignment(Element.ALIGN_CENTER);
            dataCell1.setPadding(10);

            PdfPCell dataCell2 = new PdfPCell(new Phrase(String.valueOf(sancionesActivas.size()), dataFont));
            dataCell2.setHorizontalAlignment(Element.ALIGN_CENTER);
            dataCell2.setPadding(10);

            PdfPCell dataCell3 = new PdfPCell(new Phrase(String.valueOf(sancionesResueltas.size()), dataFont));
            dataCell3.setHorizontalAlignment(Element.ALIGN_CENTER);
            dataCell3.setPadding(10);

            statsTable.addCell(dataCell1);
            statsTable.addCell(dataCell2);
            statsTable.addCell(dataCell3);

            document.add(statsTable);

            // Tabla de sanciones
            if (!sanciones.isEmpty()) {
                PdfPTable table = new PdfPTable(7);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{1, 3, 2, 3, 2, 2, 2});

                // Headers
                Font tableHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE);
                String[] headers = {"ID", "Usuario", "Tipo", "Descripción", "Multa", "Fecha", "Estado"};

                for (String header : headers) {
                    PdfPCell cell = new PdfPCell(new Phrase(header, tableHeaderFont));
                    cell.setBackgroundColor(BaseColor.DARK_GRAY);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setPadding(8);
                    table.addCell(cell);
                }

                // Data
                Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                for (Penalty sancion : sanciones) {
                    // ID
                    PdfPCell idCell = new PdfPCell(new Phrase(sancion.getId().toString(), cellFont));
                    idCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    idCell.setPadding(5);
                    table.addCell(idCell);

                    // Usuario
                    PdfPCell userCell = new PdfPCell(new Phrase(sancion.getUser().getFullName(), cellFont));
                    userCell.setPadding(5);
                    table.addCell(userCell);

                    // Tipo
                    PdfPCell typeCell = new PdfPCell(new Phrase(sancion.getType(), cellFont));
                    typeCell.setPadding(5);
                    table.addCell(typeCell);

                    // Descripción (truncada)
                    String description = sancion.getDescription();
                    if (description.length() > 50) {
                        description = description.substring(0, 47) + "...";
                    }
                    PdfPCell descCell = new PdfPCell(new Phrase(description, cellFont));
                    descCell.setPadding(5);
                    table.addCell(descCell);

                    // Multa
                    String multa = sancion.getFineAmount() != null && sancion.getFineAmount().compareTo(BigDecimal.ZERO) > 0
                            ? "$" + String.format("%.2f", sancion.getFineAmount())
                            : "Sin multa";
                    PdfPCell fineCell = new PdfPCell(new Phrase(multa, cellFont));
                    fineCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    fineCell.setPadding(5);
                    table.addCell(fineCell);

                    // Fecha
                    PdfPCell dateCell = new PdfPCell(new Phrase(sancion.getCreatedAt().format(formatter), cellFont));
                    dateCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    dateCell.setPadding(5);
                    table.addCell(dateCell);

                    // Estado
                    String estado = sancion.getIsResolved() ? "Resuelta" : "Activa";
                    PdfPCell statusCell = new PdfPCell(new Phrase(estado, cellFont));
                    statusCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    statusCell.setPadding(5);
                    if (sancion.getIsResolved()) {
                        statusCell.setBackgroundColor(new BaseColor(220, 255, 220));
                    } else {
                        statusCell.setBackgroundColor(new BaseColor(255, 245, 220));
                    }
                    table.addCell(statusCell);
                }

                document.add(table);
            } else {
                Paragraph noData = new Paragraph("No hay sanciones registradas.",
                        FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 12, BaseColor.GRAY));
                noData.setAlignment(Element.ALIGN_CENTER);
                noData.setSpacingBefore(20);
                document.add(noData);
            }

            document.close();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment",
                    "reporte_sanciones_" + java.time.LocalDate.now().toString() + ".pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(baos.toByteArray());

        } catch (DocumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // Ver detalles de una sanción específica
    @GetMapping("/admin/detalle/{id}")
    public String verDetalleSancion(@PathVariable Integer id, Model model) {
        Optional<Penalty> penaltyOpt = penaltyService.buscarPorId(id);

        if (penaltyOpt.isPresent()) {
            model.addAttribute("sancion", penaltyOpt.get());
            model.addAttribute("titulo", "Detalle de Sanción");
            return "sanciones/admin-detalle";
        }

        return "redirect:/sanciones/admin/listar";
    }

    // Filtrar sanciones por tipo
    @GetMapping("/admin/filtrar")
    public String filtrarSanciones(@RequestParam(required = false) String tipo,
                                   @RequestParam(required = false) String estado,
                                   @RequestParam(required = false) String estudiante,
                                   Model model) {

        List<Penalty> sanciones = penaltyService.listarTodas();

        if (estudiante != null && !estudiante.trim().isEmpty()) {
            String estudianteLower = estudiante.toLowerCase().trim();
            sanciones = sanciones.stream()
                    .filter(s -> s.getUser().getFullName().toLowerCase().contains(estudianteLower) ||
                            s.getUser().getEmail().toLowerCase().contains(estudianteLower))
                    .collect(Collectors.toList());
        }

        if (tipo != null && !tipo.isEmpty()) {
            sanciones = sanciones.stream()
                    .filter(s -> s.getType().equals(tipo))
                    .collect(Collectors.toList());
        }

        if ("activas".equals(estado)) {
            sanciones = sanciones.stream()
                    .filter(s -> !s.getIsResolved())
                    .collect(Collectors.toList());
        } else if ("resueltas".equals(estado)) {
            sanciones = sanciones.stream()
                    .filter(s -> s.getIsResolved())
                    .collect(Collectors.toList());
        }

        // Estadísticas para la vista filtrada
        List<Penalty> sancionesActivas = sanciones.stream()
                .filter(s -> !s.getIsResolved())
                .collect(Collectors.toList());
        List<Penalty> sancionesResueltas = sanciones.stream()
                .filter(s -> s.getIsResolved())
                .collect(Collectors.toList());

        long estudiantesBloqueados = sancionesActivas.stream()
                .map(sancion -> sancion.getUser().getId())
                .distinct()
                .count();

        model.addAttribute("todasSanciones", sanciones);
        model.addAttribute("sancionesActivas", sancionesActivas);
        model.addAttribute("sancionesResueltas", sancionesResueltas);
        model.addAttribute("estudiantesBloqueados", estudiantesBloqueados);
        model.addAttribute("tipoFiltro", tipo);
        model.addAttribute("estadoFiltro", estado);
        model.addAttribute("estudianteFiltro", estudiante);
        model.addAttribute("titulo", "Sanciones Filtradas");

        return "sanciones/admin-listar";
    }
}