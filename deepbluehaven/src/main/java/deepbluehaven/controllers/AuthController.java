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
import deepbluehaven.pojo.enums.ActionCode;
import deepbluehaven.pojo.enums.ObjectType;
import deepbluehaven.repositories.CustomerRepository;
import deepbluehaven.repositories.WorkerRepository;
import deepbluehaven.services.AuthService;
import deepbluehaven.services.LogService;
import deepbluehaven.services.RememberMeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class AuthController {

    private static final int SESSION_TIMEOUT_SECONDS = 60 * 60 * 8;

    private final AuthService authService;
    private final LogService logService;
    private final CustomerRepository customerRepository;
    private final WorkerRepository workerRepository;
    private final RememberMeService rememberMeService;

    public AuthController(AuthService authService,
                          LogService logService,
                          CustomerRepository customerRepository,
                          WorkerRepository workerRepository,
                          RememberMeService rememberMeService) {
        this.authService = authService;
        this.logService = logService;
        this.customerRepository = customerRepository;
        this.workerRepository = workerRepository;
        this.rememberMeService = rememberMeService;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "logout", required = false) String logout,
            @RequestParam(value = "error", required = false) String error,
            Model model,
            HttpServletRequest request) {

        if (logout != null || error != null) {
            return "auth/login";
        }

        HttpSession session = request.getSession(false);

        if (session != null) {
            if (session.getAttribute("loggedInCustomerId") != null) {
                return "redirect:/";
            }

            if (session.getAttribute("loggedInWorkerId") != null) {
                return redirectWorkerByRole((String) session.getAttribute("workerRole"));
            }
        }

        return "auth/login";
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.trim().isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "127.0.0.1";
    }

    @PostMapping("/login")
    public String loginCustomer(@RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam(value = "remember-me", required = false) String rememberMe,
            HttpServletRequest request,
            HttpServletResponse response) {

        String ip = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        Customer customer = authService.loginCustomer(username, password);

        if (customer == null) {
            Long existingAccountId = customerRepository.findByUsername(username).map(c -> c.getId()).orElse(null);
            logService.logAuthAccess(existingAccountId, "CUSTOMER", "LOGIN_FAILED", ip, userAgent);
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

        if (rememberMe != null) {
            rememberMeService.createRememberCustomerCookie(response, request.getContextPath(), customer.getId());
        }

        logService.logAuthAccess(customer.getId(), "CUSTOMER", "LOGIN_SUCCESS", ip, userAgent);
        logService.log(ObjectType.USER, ActionCode.CHECK_IN, customer.getId(), "Customer logged in: " + username, session);

        return "redirect:/";
    }

    @GetMapping("/staff-login")
    public String staffLoginPage(@RequestParam(value = "logout", required = false) String logout,
            @RequestParam(value = "error", required = false) String error,
            Model model,
            HttpServletRequest request) {

        if (logout != null || error != null) {
            return "auth/staff-login";
        }

        HttpSession session = request.getSession(false);

        if (session != null) {
            if (session.getAttribute("loggedInWorkerId") != null) {
                return redirectWorkerByRole((String) session.getAttribute("workerRole"));
            }

            if (session.getAttribute("loggedInCustomerId") != null) {
                return "redirect:/";
            }
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
            @RequestParam(value = "remember-me", required = false) String rememberMe,
            HttpServletRequest request,
            HttpServletResponse response) {

        String ip = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        Worker worker = authService.loginWorker(username, password);

        if (worker == null) {
            Long existingAccountId = workerRepository.findByUsername(username).map(w -> w.getId()).orElse(null);
            logService.logAuthAccess(existingAccountId, "WORKER", "LOGIN_FAILED", ip, userAgent);
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

        if (rememberMe != null) {
            rememberMeService.createRememberWorkerCookie(response, request.getContextPath(), worker.getId());
        }

        logService.logAuthAccess(worker.getId(), worker.getProfile().getRole().name(), "LOGIN_SUCCESS", ip, userAgent);
        logService.log(ObjectType.WORKER, ActionCode.CHECK_IN, worker.getId(), 
                "Staff logged in: " + username + " (" + worker.getProfile().getRole() + ")", session);

        return redirectWorkerByRole(worker.getProfile().getRole().name());
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        invalidateSession(request, response);
        return "redirect:/login?logout=true";
    }

    @PostMapping("/logout")
    public String logoutPost(HttpServletRequest request, HttpServletResponse response) {
        invalidateSession(request, response);
        return "redirect:/login?logout=true";
    }

    @GetMapping("/staff-logout")
    public String logoutStaff(HttpServletRequest request, HttpServletResponse response) {
        invalidateSession(request, response);
        return "redirect:/staff-login?logout=true";
    }

    @PostMapping("/staff-logout")
    public String logoutStaffPost(HttpServletRequest request, HttpServletResponse response) {
        invalidateSession(request, response);
        return "redirect:/staff-login?logout=true";
    }

    private void invalidateSession(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);

        if (session != null) {
            Long workerId = (Long) session.getAttribute("loggedInWorkerId");
            if (workerId != null) {
                logService.log(ObjectType.WORKER, ActionCode.CHECK_OUT, workerId, "Staff logged out", session);
            }
            session.invalidate();
        }
        rememberMeService.clearRememberMeCookies(response, request.getContextPath());
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

        logService.log(ObjectType.USER, ActionCode.CREATE, customer.getId(), 
                "New customer account registered: " + customer.getUsername(), (Long) null);

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
            String identity = getRequiredValue(request, "identity");
            authService.processForgotPassword(identity);
            return ResponseEntity.ok(Map.of("message", "OTP sent successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/api/auth/verify-otp")
    @ResponseBody
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> request) {
        try {
            String identity = getRequiredValue(request, "identity");
            String otp = getRequiredValue(request, "otp");
            authService.verifyOtp(identity, otp);

            return ResponseEntity.ok(Map.of("message", "OTP verified"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/api/auth/reset-password")
    @ResponseBody
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        try {
            String identity = getRequiredValue(request, "identity");
            String newPassword = getRequiredValue(request, "newPassword");
            authService.resetPassword(identity, newPassword);

            logService.log(ObjectType.USER, ActionCode.UPDATE, 0L, 
                    "Password reset completed for identity: " + identity, (Long) null);

            return ResponseEntity.ok(
                    Map.of("message", "Password reset successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    private String getRequiredValue(Map<String, String> request, String fieldName) {
        String value = request.get(fieldName);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        return value;
    }
}