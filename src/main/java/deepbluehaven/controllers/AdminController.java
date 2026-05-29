package deepbluehaven.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

    @GetMapping("/admin/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("activePage", "dashboard");
        return "admin/dashboard";
    }

    @GetMapping("/admin/roles")
    public String roles(Model model) {
        model.addAttribute("activePage", "roles");
        return "admin/roles";
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
        model.addAttribute("activePage", "accounts");
        return "admin/create-employee";
    }
}