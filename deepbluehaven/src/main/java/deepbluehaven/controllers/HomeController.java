package deepbluehaven.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

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
}