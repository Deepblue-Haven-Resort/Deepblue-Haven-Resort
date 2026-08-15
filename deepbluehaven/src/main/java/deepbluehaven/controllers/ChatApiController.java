package deepbluehaven.controllers;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import deepbluehaven.dto.ChatDTO;
import deepbluehaven.services.ChatService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/chat")
public class ChatApiController {

    private final ChatService chatService;

    public ChatApiController(ChatService chatService) {
        this.chatService = chatService;
    }

    // --- Customer: Get or Initialize Session ---
    @GetMapping("/customer/session")
    public ResponseEntity<ChatDTO.SessionView> getCustomerSession(
            @RequestParam(value = "guestName", required = false) String guestName,
            @RequestParam(value = "guestEmail", required = false) String guestEmail,
            HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Long customerId = (session != null && session.getAttribute("loggedInCustomerId") != null)
                ? (Long) session.getAttribute("loggedInCustomerId") : null;

        ChatDTO.SessionView sessionView = chatService.getOrCreateActiveSession(customerId, guestName, guestEmail);
        return ResponseEntity.ok(sessionView);
    }

    // --- Customer: Send Message ---
    @PostMapping("/customer/send")
    public ResponseEntity<ChatDTO.MessageView> sendCustomerMessage(
            @RequestBody ChatDTO.SendMessageRequest req,
            HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        String senderName = req.getGuestName();
        if (session != null && session.getAttribute("loggedInCustomerName") != null) {
            senderName = (String) session.getAttribute("loggedInCustomerName");
        }

        ChatDTO.MessageView msg = chatService.sendCustomerMessage(req.getSessionId(), req.getContent(), senderName);
        return ResponseEntity.ok(msg);
    }

    // --- Poll Messages for a Session ---
    @GetMapping("/messages")
    public ResponseEntity<List<ChatDTO.MessageView>> getMessages(
            @RequestParam("sessionId") Long sessionId,
            @RequestParam(value = "afterId", required = false, defaultValue = "0") Long afterId) {
        List<ChatDTO.MessageView> messages = chatService.getMessages(sessionId, afterId);
        return ResponseEntity.ok(messages);
    }

    // --- Staff: Get Sessions Queue ---
    @GetMapping("/staff/sessions")
    public ResponseEntity<List<ChatDTO.SessionView>> getStaffSessions(
            @RequestParam(value = "status", required = false) String status) {
        List<ChatDTO.SessionView> sessions = chatService.getStaffSessions(status);
        return ResponseEntity.ok(sessions);
    }

    // --- Staff: Send Message ---
    @PostMapping("/staff/send")
    public ResponseEntity<ChatDTO.MessageView> sendStaffMessage(
            @RequestBody ChatDTO.SendMessageRequest req,
            HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Long workerId = (session != null && session.getAttribute("loggedInWorkerId") != null)
                ? (Long) session.getAttribute("loggedInWorkerId") : 1L;

        ChatDTO.MessageView msg = chatService.sendStaffMessage(req.getSessionId(), workerId, req.getContent());
        return ResponseEntity.ok(msg);
    }

    // --- Staff: Assign/Join Session ---
    @PostMapping("/staff/session/{id}/assign")
    public ResponseEntity<Map<String, Object>> assignSession(
            @PathVariable("id") Long sessionId,
            HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Long workerId = (session != null && session.getAttribute("loggedInWorkerId") != null)
                ? (Long) session.getAttribute("loggedInWorkerId") : 1L;

        boolean success = chatService.assignSession(sessionId, workerId);
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", success);
        return ResponseEntity.ok(resp);
    }

    // --- Staff: Resolve Session ---
    @PostMapping("/staff/session/{id}/resolve")
    public ResponseEntity<Map<String, Object>> resolveSession(
            @PathVariable("id") Long sessionId,
            HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Long workerId = (session != null && session.getAttribute("loggedInWorkerId") != null)
                ? (Long) session.getAttribute("loggedInWorkerId") : 1L;

        boolean success = chatService.resolveSession(sessionId, workerId);
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", success);
        return ResponseEntity.ok(resp);
    }

    // --- Staff: Mark Session Read ---
    @PostMapping("/staff/session/{id}/read")
    public ResponseEntity<Map<String, Object>> markSessionRead(@PathVariable("id") Long sessionId) {
        chatService.markSessionRead(sessionId);
        return ResponseEntity.ok(Collections.singletonMap("success", true));
    }
}
