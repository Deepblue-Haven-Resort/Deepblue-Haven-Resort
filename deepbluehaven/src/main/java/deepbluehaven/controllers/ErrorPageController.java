package deepbluehaven.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorPageController {

    @GetMapping("/404")
    public String notFound() {
        return "error/404";
    }

    @GetMapping("/500")
    public String internalServerError() {
        return "error/500";
    }

    @GetMapping("/403")
    public String forbidden() {
        return "error/403";
    }
    @GetMapping("/401")
    public String unauthorized() {
        return "error/401";
    }
}