package deepbluehaven.controllers;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import deepbluehaven.dto.ServiceDTO;
import deepbluehaven.services.CustomerService;

@Controller
public class HomeController {

    private final CustomerService CustomerService;

    public HomeController(CustomerService CustomerService) {
        this.CustomerService = CustomerService;
    }

    @GetMapping("/")
    public String index() {
        return "customer/home";
    }

    @GetMapping("/home")
    public String home() {
        return "customer/home";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "customer/dashboard";
    }

    @GetMapping("/rooms")
    public String roomsList() {
        return "customer/rooms-list";
    }

    @GetMapping("/rooms/{id}")
    public String roomDetail() {
        return "customer/room-detail";
    }

    @GetMapping("/services")
    public String showServices(Model model) {
        List<ServiceDTO.Response> services = CustomerService.getVisibleServices();
        model.addAttribute("services", services);
        return "customer/service";
    }
/* vi trong code co method="post" */
    @PostMapping("/services/confirm")
    public String confirmServices() {
        return "redirect:/services";
    }
}
