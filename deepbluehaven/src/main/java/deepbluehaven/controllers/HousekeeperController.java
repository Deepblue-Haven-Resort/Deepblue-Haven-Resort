package deepbluehaven.controllers;

import java.util.ArrayList;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import deepbluehaven.dto.TaskDTO;
import deepbluehaven.pojo.Room;
import deepbluehaven.pojo.Worker;
import deepbluehaven.repositories.RoomRepository;
import deepbluehaven.services.CloudinaryStorageService;
import deepbluehaven.services.HousekeeperService;
import deepbluehaven.services.WorkerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class HousekeeperController {

    private final HousekeeperService housekeeperService;
    private final WorkerService workerService;
    private final RoomRepository roomRepository;
    private final deepbluehaven.repositories.WorkerRepository workerRepository;
    private final CloudinaryStorageService cloudinaryStorageService;

    public HousekeeperController(HousekeeperService housekeeperService,
                                 WorkerService workerService,
                                 RoomRepository roomRepository,
                                 deepbluehaven.repositories.WorkerRepository workerRepository,
                                 CloudinaryStorageService cloudinaryStorageService) {
        this.housekeeperService = housekeeperService;
        this.workerService = workerService;
        this.roomRepository = roomRepository;
        this.workerRepository = workerRepository;
        this.cloudinaryStorageService = cloudinaryStorageService;
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
    public String startTask(@PathVariable Long id, HttpSession session, HttpServletRequest req, RedirectAttributes redirectAttrs) {
        try {
            Long workerId = getLoggedInWorkerId(session);
            housekeeperService.startTask(id, workerId);
            redirectAttrs.addFlashAttribute("successMessage", "Started task #" + id + " successfully!");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", "Failed to start task: " + e.getMessage());
        }
        String referer = req.getHeader("Referer");
        if (referer != null && referer.contains("/housekeeper/tasks")) {
            return "redirect:/housekeeper/tasks";
        }
        return "redirect:/housekeeper/dashboard";
    }

    @PostMapping("/housekeeper/tasks/{id}/complete")
    public String completeTask(@PathVariable Long id,
                               @RequestParam(value = "proofImageUrl", required = false) String proofImageUrl,
                               @RequestParam(value = "proofFiles", required = false) List<MultipartFile> proofFiles,
                               HttpSession session,
                               HttpServletRequest req,
                               RedirectAttributes redirectAttrs) {
        try {
            Long workerId = getLoggedInWorkerId(session);

            if ((proofImageUrl == null || proofImageUrl.isBlank()) && proofFiles != null && !proofFiles.isEmpty()) {
                List<String> uploadedUrls = new ArrayList<>();
                for (MultipartFile file : proofFiles) {
                    if (file != null && !file.isEmpty()) {
                        try {
                            String url = cloudinaryStorageService.uploadImage(file, "deepbluehaven/housekeeping_proofs");
                            if (url != null && !url.isBlank()) {
                                uploadedUrls.add(url);
                            }
                        } catch (Exception ignored) {}
                    }
                }
                if (!uploadedUrls.isEmpty()) {
                    proofImageUrl = String.join(",", uploadedUrls);
                }
            }

            if (proofImageUrl == null || proofImageUrl.isBlank()) {
                proofImageUrl = "https://res.cloudinary.com/xio0mgix/image/upload/v1786686618/1f196b65-2daa-49b5-9a6c-f1e5e1d50a6e.png";
            }

            housekeeperService.completeTask(id, workerId, proofImageUrl);
            redirectAttrs.addFlashAttribute("successMessage", "Task #" + id + " submitted for manager inspection!");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", "Failed to complete task: " + e.getMessage());
        }
        String referer = req.getHeader("Referer");
        if (referer != null && referer.contains("/housekeeper/tasks")) {
            return "redirect:/housekeeper/tasks";
        }
        return "redirect:/housekeeper/dashboard";
    }

    @PostMapping("/housekeeper/report-issue")
    public ResponseEntity<Map<String, Object>> reportIssue(
            @RequestParam("roomNumber") String roomNumber,
            @RequestParam(value = "issueType", required = false) String issueType,
            @RequestParam(value = "priority", required = false) String priority,
            @RequestParam(value = "description", required = false) String description, HttpSession session) {
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

    @PostMapping("/housekeeper/tasks/{id}/minibar-log")
    public ResponseEntity<Map<String, Object>> logMinibarConsumption(
            @PathVariable Long id,
            @RequestParam("itemName") String itemName,
            @RequestParam(value = "quantity", defaultValue = "1") int quantity,
            HttpSession session) {
        Long workerId = getLoggedInWorkerId(session);
        try {
            housekeeperService.logMinibarConsumption(id, itemName, quantity, workerId);
            Map<String, Object> resp = new java.util.HashMap<>();
            resp.put("success", true);
            resp.put("message", "Minibar consumption recorded successfully.");
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