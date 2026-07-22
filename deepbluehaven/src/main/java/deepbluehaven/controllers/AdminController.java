package deepbluehaven.controllers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import deepbluehaven.dto.WorkerCreateFormDTO;
import deepbluehaven.dto.WorkerDTO;
import deepbluehaven.pojo.Worker;
import deepbluehaven.pojo.enums.Department;
import deepbluehaven.pojo.enums.Gender;
import deepbluehaven.pojo.enums.PermissionTag;
import deepbluehaven.pojo.enums.Role;
import deepbluehaven.pojo.enums.WorkerStatus;
import deepbluehaven.services.WorkerFormExceptionService;
import deepbluehaven.services.WorkerService;
import jakarta.validation.Valid;

@Controller
public class AdminController {

    private final WorkerService workerService;

    public AdminController(WorkerService workerService) {
        this.workerService = workerService;
    }

    @GetMapping("/admin/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("activePage", "dashboard");
        return "admin/dashboard";
    }

    @GetMapping("/admin/accounts")
    public String accounts(Model model) {
        model.addAttribute("activePage", "accounts");
        return "admin/accounts";
    }

    @GetMapping("/admin/profile-employee")
    public String profileEmployee(Model model) {
        model.addAttribute("activePage", "profile-employee");
        return "admin/profile-employee";
    }

    @GetMapping("/admin/accounts/create")
    public String createEmployeeAccount(Model model) {
        if (!model.containsAttribute("createWorkerForm")) {
            model.addAttribute("createWorkerForm", new WorkerCreateFormDTO());
        }
        addCreateWorkerOptions(model);
        return "admin/create-employee";
    }

    @PostMapping("/admin/accounts/create")
    public String createEmployeeAccount(@Valid @ModelAttribute("createWorkerForm") WorkerCreateFormDTO form, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            addCreateWorkerOptions(model);
            return "admin/create-employee";
        }

        try {
            Worker worker = workerService.createWorker(form);
            redirectAttributes.addFlashAttribute("successMessage", "Employee account created successfully");
            return "redirect:/admin/accounts?createdWorkerId="+ worker.getId();

        } catch (WorkerFormExceptionService exception) {
            bindingResult.rejectValue(exception.getField(), "worker.create.failed", exception.getMessage());
            addCreateWorkerOptions(model);
            return "admin/create-employee";
        }
    }

    private void addCreateWorkerOptions(Model model) {
        model.addAttribute("activePage", "accounts");
        model.addAttribute("allDepartments", Department.values());
        model.addAttribute("allRoles", Role.values());
        model.addAttribute("allWorkerStatuses", WorkerStatus.values());
        model.addAttribute("allGenders", Gender.values());
        model.addAttribute("allPermissions", PermissionTag.values());
    }

    @GetMapping("/admin/settings")
    public String adminSettings(Model model) {
        model.addAttribute("activePage", "settings");
        return "admin/setting-admin";
    }

    
    @GetMapping("/admin/accounts/stats")
    @ResponseBody
    public ResponseEntity<Map<String, Long>> getStats() {
        return ResponseEntity.ok(workerService.getDashboardStats());
    }
 
    @GetMapping("/admin/accounts/role-distribution")
    @ResponseBody
    public ResponseEntity<Map<String, Long>> getRoleDistribution() {
        return ResponseEntity.ok(workerService.getRoleDistribution());
    }
    
    @GetMapping("/admin/accounts/list")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> listAccounts( @RequestParam(required = false) String search, @RequestParam(required = false) Role role,
        @RequestParam(required = false) WorkerStatus status, @RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(workerService.listAccounts(search, role, status, page));
    }
 
    @PatchMapping("/admin/accounts/{id}/status")
    @ResponseBody
    public ResponseEntity<WorkerDTO.Response> updateStatus(@PathVariable Long id, @RequestParam WorkerStatus status) {
        return ResponseEntity.ok(workerService.setStatus(id, status));
    }
 
    @DeleteMapping("/admin/accounts/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteAccount(@PathVariable Long id) {
        workerService.deleteWorker(id);
        return ResponseEntity.noContent().build();
    }
}