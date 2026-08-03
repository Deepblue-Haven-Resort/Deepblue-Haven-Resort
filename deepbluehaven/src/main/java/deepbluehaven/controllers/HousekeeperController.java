package deepbluehaven.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HousekeeperController {

    @GetMapping("/housekeeper/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("activePage", "dashboard");
        return "housekeeper/dashboard";
    }

    @GetMapping("/housekeeper/tasks")
    public String tasks(Model model) {
        model.addAttribute("activePage", "tasks");
        return "housekeeper/tasks";
    }

    @GetMapping("/housekeeper/rooms")
    public String rooms(Model model) {
        model.addAttribute("activePage", "rooms");
        return "housekeeper/rooms";
    }

    @GetMapping("/housekeeper/history")
    public String history(Model model) {
        model.addAttribute("activePage", "history");
        return "housekeeper/history";
    }

    @GetMapping("/housekeeper/profile")
    public String profile(Model model) {
        model.addAttribute("activePage", "profile");
        return "housekeeper/profile";
    }

}