package deepbluehaven.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

  @GetMapping("/")
  public String dashboard() {
    return "customer/dashboard";
  }
  @GetMapping("/home")
    public String home() {
        return "customer/home";
    }
    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
