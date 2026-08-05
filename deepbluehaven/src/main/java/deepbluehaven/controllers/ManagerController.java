package deepbluehaven.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import deepbluehaven.dto.ManagerDashboardDTO;
import deepbluehaven.services.ManagerDashboardService;

@Controller
public class ManagerController {

    private final ManagerDashboardService managerDashboardService;

    public ManagerController(ManagerDashboardService managerDashboardService) {
        this.managerDashboardService = managerDashboardService;
    }

    @GetMapping("/manager/dashboard")
    public String managerDashboard(Model model) {
        ManagerDashboardDTO dashboardData = managerDashboardService.getDashboardData();
        model.addAttribute("dashboardData", dashboardData);
        model.addAttribute("activePage", "dashboard");

        return "manager/dashboard";
    }

}