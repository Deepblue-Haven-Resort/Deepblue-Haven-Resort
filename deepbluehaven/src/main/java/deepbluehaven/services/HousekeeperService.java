package deepbluehaven.services;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import deepbluehaven.dto.TaskDTO;
import deepbluehaven.pojo.Room;
import deepbluehaven.pojo.Task;
import deepbluehaven.pojo.enums.RoomStatus;
import deepbluehaven.pojo.enums.TaskStatus;
import deepbluehaven.repositories.RoomRepository;
import deepbluehaven.repositories.TaskRepository;

import deepbluehaven.pojo.Worker;
import deepbluehaven.pojo.enums.ActionCode;
import deepbluehaven.pojo.enums.ObjectType;
import deepbluehaven.repositories.WorkerRepository;

@Service
public class HousekeeperService {

    private final TaskRepository taskRepository;
    private final RoomRepository roomRepository;
    private final WorkerRepository workerRepository;
    private final LogService logService;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");

    public HousekeeperService(TaskRepository taskRepository, RoomRepository roomRepository,
                              WorkerRepository workerRepository, LogService logService) {
        this.taskRepository = taskRepository;
        this.roomRepository = roomRepository;
        this.workerRepository = workerRepository;
        this.logService = logService;
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getDashboardStats(Long workerId) {
        Map<String, Long> stats = new HashMap<>();
        stats.put("pendingCount", taskRepository.countByAssignedToIdAndStatus(workerId, TaskStatus.PENDING));
        stats.put("cleaningCount", taskRepository.countByAssignedToIdAndStatus(workerId, TaskStatus.CLEANING));
        stats.put("maintenanceCount", taskRepository.countByAssignedToIdAndStatus(workerId, TaskStatus.ASSIGNED));
        stats.put("completedCount", taskRepository.countByAssignedToIdAndStatus(workerId, TaskStatus.INSPECTED));
        return stats;
    }

    @Transactional(readOnly = true)
    public List<TaskDTO.Response> getTodayTasks(Long workerId) {
        List<Task> tasks = taskRepository.findByAssignedToIdOrderByTimestampDesc(workerId);
        List<TaskDTO.Response> result = new ArrayList<>();
        for (Task task : tasks) {
            result.add(mapToTaskDTO(task));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<TaskDTO.Response> getTaskHistory(Long workerId) {
        List<Task> tasks = taskRepository.findByAssignedToIdAndStatusOrderByTimestampDesc(workerId, TaskStatus.INSPECTED);
        List<TaskDTO.Response> result = new ArrayList<>();
        for (Task task : tasks) {
            result.add(mapToTaskDTO(task));
        }
        return result;
    }

    @Transactional
    public void startTask(Long taskId, Long workerId) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new IllegalArgumentException("Task not found"));
        task.setStatus(TaskStatus.CLEANING);
        taskRepository.save(task);

        Worker worker = workerId != null ? workerRepository.findById(workerId).orElse(null) : null;

        if (task.getRoom() != null) {
            Room room = task.getRoom();
            RoomStatus oldStatus = room.getStatus();
            room.setStatus(RoomStatus.CLEANING);
            roomRepository.save(room);
            logService.logRoomStatusChange(room, oldStatus, RoomStatus.CLEANING, worker);
        }

        logService.log(ObjectType.WORKER, ActionCode.UPDATE, taskId,
                "Housekeeper started cleaning task #" + taskId + " for Room #" + (task.getRoom() != null ? task.getRoom().getRoomNumber() : "N/A"), workerId);
    }

    @Transactional
    public void completeTask(Long taskId, Long workerId) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new IllegalArgumentException("Task not found"));
        task.setStatus(TaskStatus.INSPECTED);
        taskRepository.save(task);

        Worker worker = workerId != null ? workerRepository.findById(workerId).orElse(null) : null;

        if (task.getRoom() != null) {
            Room room = task.getRoom();
            RoomStatus oldStatus = room.getStatus();
            room.setStatus(RoomStatus.AVAILABLE);
            roomRepository.save(room);
            logService.logRoomStatusChange(room, oldStatus, RoomStatus.AVAILABLE, worker);
        }

        logService.log(ObjectType.WORKER, ActionCode.UPDATE, taskId,
                "Housekeeper completed cleaning task #" + taskId + " for Room #" + (task.getRoom() != null ? task.getRoom().getRoomNumber() : "N/A"), workerId);
    }

    @Transactional(readOnly = true)
    public String exportTaskHistoryCsv(Long workerId) {
        List<TaskDTO.Response> history = getTaskHistory(workerId);
        StringBuilder csv = new StringBuilder();
        csv.append("Log ID,Room Number,Room Type,Task Action,Completion Time,Inspector,Result\n");
        for (TaskDTO.Response item : history) {
            csv.append(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                    item.getTaskCode(),
                    item.getRoomNumber(),
                    item.getRoomType(),
                    item.getAction(),
                    item.getTimeText(),
                    item.getAssignedByName() != null ? item.getAssignedByName() : "Manager",
                    item.getResultText() != null ? item.getResultText() : "Passed"
            ));
        }
        return csv.toString();
    }

    private TaskDTO.Response mapToTaskDTO(Task task) {
        TaskDTO.Response dto = new TaskDTO.Response();
        dto.setId(task.getId());
        dto.setTaskCode("#HK-" + (100 + task.getId()));
        
        if (task.getRoom() != null) {
            dto.setRoomId(task.getRoom().getId());
            dto.setRoomNumber("Room " + task.getRoom().getRoomNumber());
            dto.setRoomType(task.getRoom().getRoomType() != null ? task.getRoom().getRoomType().name() : "Standard");
        } else {
            dto.setRoomNumber("N/A");
            dto.setRoomType("Standard");
        }

        dto.setAction(task.getAction() != null ? task.getAction() : "Routine Cleaning");
        dto.setStatus(task.getStatus());
        dto.setStatusLabel(task.getStatus() != null ? task.getStatus().name() : "PENDING");
        dto.setStatusClass(resolveStatusClass(task.getStatus()));
        if (task.getRoom() != null && task.getRoom().getRoomType() != null) {
            String rType = task.getRoom().getRoomType().name();
            if ("STANDARD".equalsIgnoreCase(rType)) {
                dto.setPriority("Low");
                dto.setPriorityClass("admin");
            } else if ("SUPERIOR".equalsIgnoreCase(rType) || "DELUXE".equalsIgnoreCase(rType)) {
                dto.setPriority("Medium");
                dto.setPriorityClass("manager");
            } else {
                dto.setPriority("High");
                dto.setPriorityClass("housekeeper"); 
            }
        } else {
            dto.setPriority("Low");
            dto.setPriorityClass("admin");
        }

        if (task.getTimestamp() != null) {
            dto.setTimeText(task.getTimestamp().format(TIME_FORMATTER));
        } else {
            dto.setTimeText("10:00 AM");
        }

        if (task.getAssignedBy() != null && task.getAssignedBy().getProfile() != null) {
            dto.setAssignedByName(task.getAssignedBy().getProfile().getFullName() + " (Manager)");
        } else {
            dto.setAssignedByName("Tran Hoang Nam (Manager)");
        }

        dto.setResultText("Passed");
        return dto;
    }

    private String resolveStatusClass(TaskStatus status) {
        if (status == null) return "status-warning";
        switch (status) {
            case CLEANING: 
                return "status-info";
            case INSPECTED: 
                return "status-success";
            case ASSIGNED: 
                return "status-error";
            case PENDING:
            default: return "status-warning";
        }
    }
}
