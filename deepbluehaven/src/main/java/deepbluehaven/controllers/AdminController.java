package deepbluehaven.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import deepbluehaven.dto.WorkerCreateFormDTO;
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
            model.addAttribute(
                    "createWorkerForm",
                    new WorkerCreateFormDTO());
        }

        addCreateWorkerOptions(model);
        return "admin/create-employee";
    }

    @PostMapping("/admin/accounts/create")
    public String createEmployeeAccount(
            @Valid @ModelAttribute("createWorkerForm") WorkerCreateFormDTO form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            addCreateWorkerOptions(model);
            return "admin/create-employee";
        }

        try {
            Worker worker = workerService.createWorker(form);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Employee account created successfully");

            return "redirect:/admin/accounts?createdWorkerId="
                    + worker.getId();

        } catch (WorkerFormExceptionService exception) {
            bindingResult.rejectValue(
                    exception.getField(),
                    "worker.create.failed",
                    exception.getMessage());

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
}