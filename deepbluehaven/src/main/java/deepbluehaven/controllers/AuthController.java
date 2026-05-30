package deepbluehaven.controllers;

import deepbluehaven.pojo.Customer;
import deepbluehaven.pojo.Worker;
import deepbluehaven.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private static final int SESSION_TIMEOUT_SECONDS = 60 * 60 * 8;

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "logout", required = false) String logout,
                            @RequestParam(value = "error", required = false) String error,
                            HttpServletRequest request) {

        if (logout != null || error != null) {
            return "auth/login";
        }

        HttpSession session = request.getSession(false);

        if (session != null) {
            Object workerId = session.getAttribute("loggedInWorkerId");
            Object customerId = session.getAttribute("loggedInCustomerId");

            if (workerId != null) {
                String role = (String) session.getAttribute("workerRole");

                if ("ADMIN".equals(role)) {
                    return "redirect:/admin/dashboard";
                }

                if ("MANAGER".equals(role)) {
                    return "redirect:/manager/dashboard";
                }

                if ("RECEPTIONIST".equals(role)) {
                    return "redirect:/receptionist/dashboard";
                }

                if ("HOUSEKEEPER".equals(role)) {
                    return "redirect:/housekeeper/dashboard";
                }
            }

            if (customerId != null) {
                return "redirect:/home";
            }
        }

        return "auth/login";
    }

    @PostMapping("/login")
    public String loginCustomer(@RequestParam("username") String username,
                                @RequestParam("password") String password,
                                HttpServletRequest request) {

        Customer customer = authService.loginCustomer(username, password);

        if (customer == null) {
            return "redirect:/login?error=true";
        }

        HttpSession oldSession = request.getSession(false);

        if (oldSession != null) {
            oldSession.invalidate();
        }

        HttpSession session = request.getSession(true);

        session.setAttribute("loggedInCustomerId", customer.getId());
        session.setAttribute("customerName", customer.getProfile().getFullName());
        session.setMaxInactiveInterval(SESSION_TIMEOUT_SECONDS);

        return "redirect:/home";
    }

    @PostMapping("/staff-login")
    public String loginStaff(@RequestParam("username") String username,
                             @RequestParam("password") String password,
                             HttpServletRequest request) {

        Worker worker = authService.loginWorker(username, password);

        if (worker == null) {
            return "redirect:/login?error=true";
        }

        HttpSession oldSession = request.getSession(false);

        if (oldSession != null) {
            oldSession.invalidate();
        }

        HttpSession session = request.getSession(true);

        session.setAttribute("loggedInWorkerId", worker.getId());
        session.setAttribute("workerRole", worker.getProfile().getRole().name());
        session.setAttribute("workerName", worker.getProfile().getFullName());
        session.setMaxInactiveInterval(SESSION_TIMEOUT_SECONDS);

        return switch (worker.getProfile().getRole()) {
            case ADMIN -> "redirect:/admin/dashboard";
            case MANAGER -> "redirect:/manager/dashboard";
            case RECEPTIONIST -> "redirect:/receptionist/dashboard";
            case HOUSEKEEPER -> "redirect:/housekeeper/dashboard";
        };
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        invalidateSession(request);
        return "redirect:/login?logout=true";
    }

    @PostMapping("/logout")
    public String logoutPost(HttpServletRequest request) {
        invalidateSession(request);
        return "redirect:/login?logout=true";
    }

    private void invalidateSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }
    }
}