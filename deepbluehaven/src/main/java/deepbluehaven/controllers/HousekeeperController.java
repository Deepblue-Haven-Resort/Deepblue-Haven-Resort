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

@Controller
public class HousekeeperController {

    private final HousekeeperService housekeeperService;
    private final WorkerService workerService;

    public HousekeeperController(HousekeeperService housekeeperService, WorkerService workerService) {
        this.housekeeperService = housekeeperService;
        this.workerService = workerService;
    }

    private Long getLoggedInWorkerId(HttpSession session) {
        if (session != null && session.getAttribute("loggedInWorkerId") != null) {
            return (Long) session.getAttribute("loggedInWorkerId");
        }
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

    @GetMapping("/housekeeper/history/export")
    public ResponseEntity<String> exportHistoryCsv(HttpSession session) {
        Long workerId = getLoggedInWorkerId(session);
        String csvContent = housekeeperService.exportTaskHistoryCsv(workerId);

        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=housekeeping-log.csv").contentType(MediaType.parseMediaType("text/csv")).body(csvContent);
    }
}