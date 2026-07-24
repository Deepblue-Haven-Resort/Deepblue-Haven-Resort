package deepbluehaven.controllers;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import deepbluehaven.dto.RegisterDTO;
import deepbluehaven.pojo.Customer;
import deepbluehaven.pojo.Worker;
import deepbluehaven.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

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
            Object customerId = session.getAttribute("loggedInCustomerId");

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

    @GetMapping("/staff-login")
    public String staffLoginPage(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session == null) {
            return "auth/staff-login";
        }

        if (session.getAttribute("loggedInWorkerId") != null) {
            return redirectWorkerByRole((String) session.getAttribute("workerRole"));
        }

        if (session.getAttribute("loggedInCustomerId") != null) {
            return "redirect:/home";
        }

        return "auth/staff-login";
    }

    private String redirectWorkerByRole(String role) {
        if (role == null) {
            return "redirect:/staff-login";
        }

        return switch (role) {
            case "ADMIN" -> "redirect:/admin/dashboard";
            case "MANAGER" -> "redirect:/manager/dashboard";
            case "RECEPTIONIST" -> "redirect:/receptionist/dashboard";
            case "HOUSEKEEPER" -> "redirect:/housekeeper/dashboard";
            default -> "redirect:/staff-login?error=invalid-role";
        };
    }

    @PostMapping("/staff-login")
    public String loginStaff(@RequestParam("username") String username,
            @RequestParam("password") String password,
            HttpServletRequest request) {

        Worker worker = authService.loginWorker(username, password);

        if (worker == null) {
            return "redirect:/staff-login?error=true";
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

        return redirectWorkerByRole(worker.getProfile().getRole().name());
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

    @GetMapping("/staff-logout")
    public String logoutStaff(HttpServletRequest request) {
        invalidateSession(request);
        return "redirect:/staff-login?logout=true";
    }

    @PostMapping("/staff-logout")
    public String logoutStaffPost(HttpServletRequest request) {
        invalidateSession(request);
        return "redirect:/staff-login?logout=true";
    }

    private void invalidateSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        if (!model.containsAttribute("registerForm")) {
            model.addAttribute("registerForm", new RegisterDTO.Request());
        }
        return "auth/register";
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterDTO.Request request) {
        Customer customer = authService.registerCustomer(request);

        Map<String, Object> body = Map.of(
                "id", customer.getId(),
                "username", customer.getUsername(),
                "message", "Registration successful");

        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PostMapping("/api/auth/forgot-password")
    @ResponseBody
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        try {
            authService.processForgotPassword(request.get("identity"));
            return ResponseEntity.ok(Map.of("message", "OTP sent successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/api/auth/verify-otp")
    @ResponseBody
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> request) {
        try {
            authService.verifyOtp(request.get("identity"), request.get("otp"));
            return ResponseEntity.ok(Map.of("message", "OTP verified"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/api/auth/reset-password")
    @ResponseBody
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        try {
            authService.resetPassword(request.get("identity"), request.get("newPassword"));
            return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}