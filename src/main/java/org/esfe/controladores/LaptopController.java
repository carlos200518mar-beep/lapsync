package org.esfe.controladores;

import org.esfe.modelos.Laptop;
import org.esfe.modelos.Loans;
import org.esfe.modelos.DamageReport;
import org.esfe.servicios.implementaciones.LaptopService;
import org.esfe.servicios.implementaciones.LoansServiceImpl;
import org.esfe.servicios.implementaciones.DamageReportService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/laptops")
public class LaptopController {

    @Autowired
    private LaptopService laptopService;

    @Autowired
    private LoansServiceImpl loansService;

    @Autowired
    private DamageReportService damageReportService;

    // ===== Listado general =====
    @GetMapping
    public String listLaptops(Model model) {
        model.addAttribute("laptops", laptopService.getAllLaptops());
        return "laptops/list";
    }

    // ===== Form de creación =====
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        Laptop l = new Laptop();
        // ✅ Valor por defecto para pasar la validación @NotBlank/@Pattern
        l.setStatus("available");
        model.addAttribute("laptop", l);
        return "laptops/create";
    }

    // ===== Crear laptop =====
    @PostMapping
    public String createLaptop(@Valid @ModelAttribute("laptop") Laptop laptop,
                               BindingResult result,
                               RedirectAttributes redirectAttributes,
                               Model model) {

        // Si por alguna razón viene vacío, lo forzamos (seguro extra)
        if (laptop.getStatus() == null || laptop.getStatus().isBlank()) {
            laptop.setStatus("available");
        }

        if (result.hasErrors()) {
            // Puedes mostrar un mensaje general si quieres
            model.addAttribute("error", "Revisa los datos del formulario.");
            return "laptops/create";
        }

        if (laptopService.existsByAssetTag(laptop.getAssetTag())) {
            model.addAttribute("error", "El código (Asset Tag) ya existe en el sistema.");
            return "laptops/create";
        }

        try {
            // Dejamos que @PrePersist/@PreUpdate manejen createdAt/updatedAt
            laptopService.saveLaptop(laptop);
        } catch (DataIntegrityViolationException ex) {
            // Por si falla la restricción UNIQUE a pesar del existsBy...
            model.addAttribute("error", "El código (Asset Tag) ya está registrado.");
            return "laptops/create";
        }

        redirectAttributes.addFlashAttribute("success", "Laptop agregada correctamente.");
        return "redirect:/laptops";
    }

    // ===== Form de edición =====
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Integer id,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        Optional<Laptop> opt = laptopService.getLaptopById(id);
        if (opt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Laptop no encontrada.");
            return "redirect:/laptops";
        }
        model.addAttribute("laptop", opt.get());
        return "laptops/edit";
    }

    // ===== Guardar cambios =====
    @PostMapping("/update/{id}")
    public String updateLaptop(@PathVariable("id") Integer id,
                               @Valid @ModelAttribute("laptop") Laptop laptop,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "laptops/edit";
        }

        Optional<Laptop> existingOpt = laptopService.getLaptopById(id);
        if (existingOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Laptop no encontrada.");
            return "redirect:/laptops";
        }

        Laptop existing = existingOpt.get();

        if (!existing.getAssetTag().equals(laptop.getAssetTag())
                && laptopService.existsByAssetTag(laptop.getAssetTag())) {
            model.addAttribute("error", "El código (Asset Tag) ya existe en el sistema.");
            return "laptops/edit";
        }

        // preservamos id y fechas (updatedAt se setea en @PreUpdate)
        laptop.setId(id);
        laptop.setCreatedAt(existing.getCreatedAt());

        try {
            laptopService.saveLaptop(laptop);
        } catch (DataIntegrityViolationException ex) {
            model.addAttribute("error", "El código (Asset Tag) ya está registrado.");
            return "laptops/edit";
        }

        redirectAttributes.addFlashAttribute("success", "Laptop actualizada correctamente.");
        return "redirect:/laptops";
    }

    // ===== Eliminación lógica =====
    @PostMapping("/{id}/deactivate")
    public String deactivate(@PathVariable("id") Integer id,
                             RedirectAttributes redirectAttributes) {
        try {
            laptopService.deactivateLaptop(id);
            redirectAttributes.addFlashAttribute("success", "Laptop marcada como fuera de uso.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "No se pudo marcar como fuera de uso.");
        }
        return "redirect:/laptops";
    }

    // ===== Historial por laptop =====
    @GetMapping("/{id}/history")
    public String history(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Laptop> opt = laptopService.getLaptopById(id);
        if (opt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Laptop no encontrada.");
            return "redirect:/laptops";
        }
        Laptop laptop = opt.get();

        List<Loans> loans = loansService.listarTodos()
                .stream()
                .filter(l -> l.getLaptop() != null && l.getLaptop().getId().equals(id))
                .toList();

        List<DamageReport> damages = damageReportService.getReportsForLaptop(id);

        model.addAttribute("laptop", laptop);
        model.addAttribute("loans", loans);
        model.addAttribute("damages", damages);

        return "laptops/history";
    }
}
