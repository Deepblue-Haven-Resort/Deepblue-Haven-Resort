package deepbluehaven.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import deepbluehaven.services.CustomerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/favorites")
public class CustomerFavoritesRestController {

    private final CustomerService customerService;

    public CustomerFavoritesRestController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping("/room/toggle")
    public ResponseEntity<Map<String, Object>> toggleRoomFavorite(
            @RequestParam("roomId") Long roomId,
            HttpServletRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedInCustomerId") == null) {
            response.put("success", false);
            response.put("authenticated", false);
            response.put("message", "Please log in to save your favorite rooms.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        Long customerId = (Long) session.getAttribute("loggedInCustomerId");
        try {
            boolean isFavorite = customerService.toggleFavoriteRoom(customerId, roomId);
            response.put("success", true);
            response.put("authenticated", true);
            response.put("isFavorite", isFavorite);
            response.put("message", isFavorite ? "Added room to favorites" : "Removed room from favorites");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/service/toggle")
    public ResponseEntity<Map<String, Object>> toggleServiceFavorite(
            @RequestParam("serviceId") Long serviceId,
            HttpServletRequest request) {

        Map<String, Object> response = new HashMap<>();
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedInCustomerId") == null) {
            response.put("success", false);
            response.put("authenticated", false);
            response.put("message", "Please log in to save your favorite services.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        Long customerId = (Long) session.getAttribute("loggedInCustomerId");
        try {
            boolean isFavorite = customerService.toggleFavoriteService(customerId, serviceId);
            response.put("success", true);
            response.put("authenticated", true);
            response.put("isFavorite", isFavorite);
            response.put("message", isFavorite ? "Added service to favorites" : "Removed service from favorites");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
