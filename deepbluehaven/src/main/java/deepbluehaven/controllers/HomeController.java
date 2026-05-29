package deepbluehaven.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

  @GetMapping("/dashboard")
  public String dashboard() {
    return "customer/dashboard";
  }
  @GetMapping("/")
    public String home() {
        return "customer/home";
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
