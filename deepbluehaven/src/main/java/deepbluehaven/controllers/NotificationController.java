package deepbluehaven.controllers;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import deepbluehaven.dto.NotificationDTO;
import deepbluehaven.services.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/customer/notifications")
    public String customerNotificationsPage(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedInCustomerId") == null) {
            return "redirect:/login";
        }
        model.addAttribute("activePage", "notifications");
        return "customer/notifications";
    }

    @GetMapping("/housekeeper/notifications")
    public String housekeeperNotificationsPage(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedInWorkerId") == null) {
            return "redirect:/staff-login";
        }
        model.addAttribute("activePage", "notifications");
        return "housekeeper/notifications";
    }

    @GetMapping("/api/notifications")
    @ResponseBody
    public ResponseEntity<NotificationDTO.ListResponse> getNotifications(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Long customerId = (Long) session.getAttribute("loggedInCustomerId");
            if (customerId != null) {
                return ResponseEntity.ok(notificationService.getNotificationsForCustomer(customerId));
            }
            Long workerId = (Long) session.getAttribute("loggedInWorkerId");
            if (workerId != null) {
                return ResponseEntity.ok(notificationService.getNotificationsForWorker(workerId));
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping("/api/notifications/{id}/read")
    @ResponseBody
    public ResponseEntity<Map<String, String>> markAsRead(@PathVariable Long id, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || (session.getAttribute("loggedInCustomerId") == null && session.getAttribute("loggedInWorkerId") == null)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not logged in");
        }
        notificationService.markAsRead(id);
        return ResponseEntity.ok(Map.of("message", "Marked as read"));
    }

    @PostMapping("/api/notifications/read-all")
    @ResponseBody
    public ResponseEntity<Map<String, String>> markAllAsRead(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Long customerId = (Long) session.getAttribute("loggedInCustomerId");
            if (customerId != null) {
                notificationService.markAllAsReadForCustomer(customerId);
                return ResponseEntity.ok(Map.of("message", "All customer notifications marked as read"));
            }
            Long workerId = (Long) session.getAttribute("loggedInWorkerId");
            if (workerId != null) {
                notificationService.markAllAsReadForWorker(workerId);
                return ResponseEntity.ok(Map.of("message", "All worker notifications marked as read"));
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @GetMapping("/api/notifications/realtime-badges")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getRealtimeBadges(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Map<String, Object> resp = new java.util.HashMap<>();
        long unreadCount = 0;
        if (session != null) {
            Long customerId = (Long) session.getAttribute("loggedInCustomerId");
            if (customerId != null) {
                try {
                    unreadCount = notificationService.getNotificationsForCustomer(customerId).getUnreadCount();
                } catch (Exception ignored) {}
            }
            Long workerId = (Long) session.getAttribute("loggedInWorkerId");
            if (workerId != null) {
                try {
                    unreadCount = notificationService.getNotificationsForWorker(workerId).getUnreadCount();
                } catch (Exception ignored) {}
            }
        }
        resp.put("unreadNotifications", unreadCount);
        return ResponseEntity.ok(resp);
    }
}
