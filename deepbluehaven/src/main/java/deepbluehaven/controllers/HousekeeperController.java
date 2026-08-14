package deepbluehaven.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import deepbluehaven.dto.TaskDTO;
import deepbluehaven.pojo.Worker;
import deepbluehaven.services.HousekeeperService;
import deepbluehaven.services.WorkerService;
import jakarta.servlet.http.HttpSession;

import deepbluehaven.pojo.Room;
import deepbluehaven.repositories.RoomRepository;

@Controller
public class HousekeeperController {

    private final HousekeeperService housekeeperService;
    private final WorkerService workerService;
    private final RoomRepository roomRepository;
    private final deepbluehaven.repositories.WorkerRepository workerRepository;

    public HousekeeperController(HousekeeperService housekeeperService, WorkerService workerService, RoomRepository roomRepository, deepbluehaven.repositories.WorkerRepository workerRepository) {
        this.housekeeperService = housekeeperService;
        this.workerService = workerService;
        this.roomRepository = roomRepository;
        this.workerRepository = workerRepository;
    }

    private Long getLoggedInWorkerId(HttpSession session) {
        if (session != null && session.getAttribute("loggedInWorkerId") != null) {
            return (Long) session.getAttribute("loggedInWorkerId");
        }
        try {
            List<Worker> housekeepers = workerRepository.findAll().stream()
                    .filter(w -> w.getProfile() != null && w.getProfile().getRole() == deepbluehaven.pojo.enums.Role.HOUSEKEEPER)
                    .toList();
            if (!housekeepers.isEmpty()) {
                return housekeepers.get(0).getId();
            }
        } catch (Exception ignored) {}
        return 1L;
    }

    @GetMapping("/housekeeper/dashboard")
    public String dashboard(Model model, HttpSession session) {
        Long workerId = getLoggedInWorkerId(session);
        Map<String, Long> stats = housekeeperService.getDashboardStats(workerId);
        List<TaskDTO.Response> todayTasks = housekeeperService.getTodayTasks(workerId);
        Worker worker = workerService.getWorker(workerId);

        model.addAttribute("activePage", "dashboard");
        model.addAttribute("stats", stats);
        model.addAttribute("todayTasks", todayTasks);
        model.addAttribute("worker", worker);
        return "housekeeper/dashboard";
    }

    @GetMapping("/housekeeper/tasks")
    public String tasks(Model model, HttpSession session) {
        Long workerId = getLoggedInWorkerId(session);
        List<TaskDTO.Response> todayTasks = housekeeperService.getTodayTasks(workerId);
        model.addAttribute("activePage", "tasks");
        model.addAttribute("todayTasks", todayTasks);
        return "housekeeper/tasks";
    }

    @GetMapping("/housekeeper/rooms")
    public String rooms(Model model) {
        List<Room> rooms = roomRepository.findAll();
        model.addAttribute("rooms", rooms);
        model.addAttribute("activePage", "rooms");
        return "housekeeper/rooms";
    }

    @GetMapping("/housekeeper/history")
    public String history(Model model, HttpSession session) {
        Long workerId = getLoggedInWorkerId(session);
        List<TaskDTO.Response> historyTasks = housekeeperService.getTaskHistory(workerId);

        model.addAttribute("activePage", "history");
        model.addAttribute("historyTasks", historyTasks);
        return "housekeeper/history";
    }

    @GetMapping("/housekeeper/profile")
    public String profile(Model model, HttpSession session) {
        Long workerId = getLoggedInWorkerId(session);
        Worker worker = workerService.getWorker(workerId);

        model.addAttribute("activePage", "profile");
        model.addAttribute("worker", worker);
        return "housekeeper/profile";
    }

    @GetMapping("/housekeeper/settings")
    public String housekeeperSettings(Model model) {
        model.addAttribute("activePage", "settings");
        return "housekeeper/settings";
    }

    @PostMapping("/housekeeper/tasks/{id}/start")
    public String startTask(@PathVariable Long id, HttpSession session) {
        Long workerId = getLoggedInWorkerId(session);
        housekeeperService.startTask(id, workerId);
        return "redirect:/housekeeper/dashboard";
    }

    @PostMapping("/housekeeper/tasks/{id}/complete")
    public String completeTask(@PathVariable Long id, HttpSession session) {
        Long workerId = getLoggedInWorkerId(session);
        housekeeperService.completeTask(id, workerId);
        return "redirect:/housekeeper/dashboard";
    }

    @PostMapping("/housekeeper/report-issue")
    public ResponseEntity<Map<String, Object>> reportIssue(
            @org.springframework.web.bind.annotation.RequestParam("roomNumber") String roomNumber,
            @org.springframework.web.bind.annotation.RequestParam(value = "issueType", required = false) String issueType,
            @org.springframework.web.bind.annotation.RequestParam(value = "priority", required = false) String priority,
            @org.springframework.web.bind.annotation.RequestParam(value = "description", required = false) String description,
            HttpSession session) {
        Long workerId = getLoggedInWorkerId(session);
        try {
            housekeeperService.reportRoomIssue(roomNumber, issueType, priority, description, workerId);
            Map<String, Object> resp = new java.util.HashMap<>();
            resp.put("success", true);
            resp.put("message", "Issue reported successfully. Room status set to MAINTENANCE.");
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            Map<String, Object> resp = new java.util.HashMap<>();
            resp.put("success", false);
            resp.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(resp);
        }
    }

    @GetMapping("/housekeeper/history/export")
    public ResponseEntity<String> exportHistoryCsv(HttpSession session) {
        Long workerId = getLoggedInWorkerId(session);
        String csvContent = housekeeperService.exportTaskHistoryCsv(workerId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=housekeeping-log.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvContent);
    }
}