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
    private final WorkerPerformanceService workerPerformanceService;
    private final jakarta.persistence.EntityManager entityManager;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");

    public HousekeeperService(TaskRepository taskRepository, RoomRepository roomRepository,
                              WorkerRepository workerRepository, LogService logService,
                              WorkerPerformanceService workerPerformanceService,
                              jakarta.persistence.EntityManager entityManager) {
        this.taskRepository = taskRepository;
        this.roomRepository = roomRepository;
        this.workerRepository = workerRepository;
        this.logService = logService;
        this.workerPerformanceService = workerPerformanceService;
        this.entityManager = entityManager;
    }

    @Transactional
    public void logMinibarConsumption(Long taskId, String itemName, int quantity, Long workerId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));

        Room room = task.getRoom();
        if (room == null) {
            throw new IllegalStateException("Task is not associated with a valid room.");
        }

        deepbluehaven.pojo.Booking activeBooking = null;
        try {
            List<deepbluehaven.pojo.BookingDetail> details = entityManager.createQuery(
                "select bd from BookingDetail bd join fetch bd.booking b where bd.room.id = :rId and b.status = :st", deepbluehaven.pojo.BookingDetail.class)
                .setParameter("rId", room.getId())
                .setParameter("st", deepbluehaven.pojo.enums.BookingStatus.CHECKED_IN)
                .getResultList();
            if (!details.isEmpty()) {
                activeBooking = details.get(0).getBooking();
            }
        } catch (Exception ignored) {}

        deepbluehaven.pojo.Service s = null;
        try {
            s = entityManager.createQuery(
                "select s from Service s where lower(s.name) like lower(:name)", deepbluehaven.pojo.Service.class)
                .setParameter("name", "%" + itemName.trim() + "%")
                .getResultStream().findFirst().orElse(null);
        } catch (Exception ignored) {}

        deepbluehaven.pojo.ServiceOrder so = new deepbluehaven.pojo.ServiceOrder();
        so.setService(s);
        so.setBooking(activeBooking);
        so.setCustomer(activeBooking != null ? activeBooking.getCustomer() : null);
        so.setQuantity(Math.max(1, quantity));
        java.math.BigDecimal unitPrice = (s != null && s.getBasePrice() != null) ? s.getBasePrice() : new java.math.BigDecimal("50000");
        so.setTotalPrice(unitPrice.multiply(java.math.BigDecimal.valueOf(so.getQuantity())));
        so.setNote("Minibar consumption recorded by Housekeeping (Room " + room.getRoomNumber() + ")");
        so.setAction("Minibar: " + itemName);
        so.setStatus(deepbluehaven.pojo.enums.ServiceOrderStatus.COMPLETED);
        so.setOrderTime(java.time.LocalDateTime.now());
        so.setCompletedTime(java.time.LocalDateTime.now());
        if (workerId != null) {
            so.setProcessedBy(entityManager.find(Worker.class, workerId));
        }
        entityManager.persist(so);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getDashboardStats(Long workerId) {
        Map<String, Long> stats = new HashMap<>();
        long pending = taskRepository.countByAssignedToIdAndStatus(workerId, TaskStatus.PENDING) 
                     + taskRepository.countByAssignedToIdAndStatus(workerId, TaskStatus.ASSIGNED);
        stats.put("pendingCount", pending);
        stats.put("cleaningCount", taskRepository.countByAssignedToIdAndStatus(workerId, TaskStatus.CLEANING));
        stats.put("maintenanceCount", taskRepository.countByAssignedToIdAndStatus(workerId, TaskStatus.WAITING_INSPECTION));
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
        completeTask(taskId, workerId, null);
    }

    @Transactional
    public void completeTask(Long taskId, Long workerId, String proofImageUrl) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new IllegalArgumentException("Task not found"));
        task.setStatus(TaskStatus.WAITING_INSPECTION);
        if (proofImageUrl != null && !proofImageUrl.isBlank()) {
            task.setProofImageUrl(proofImageUrl.trim());
        }
        taskRepository.save(task);

        Worker worker = workerId != null ? workerRepository.findById(workerId).orElse(null) : null;

        if (task.getRoom() != null) {
            Room room = task.getRoom();
            RoomStatus oldStatus = room.getStatus();
            room.setStatus(RoomStatus.CLEANING);
            roomRepository.save(room);
            logService.logRoomStatusChange(room, oldStatus, RoomStatus.CLEANING, worker);
        }

        if (worker != null) {
            workerPerformanceService.adjustWorkerPerformanceScore(worker, 1.5);
        }

        logService.log(ObjectType.WORKER, ActionCode.UPDATE, taskId,
                "Housekeeper completed cleaning task #" + taskId + " (Waiting Inspection) for Room #" + (task.getRoom() != null ? task.getRoom().getRoomNumber() : "N/A"), workerId);
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

    @Transactional
    public void reportRoomIssue(String roomNumber, String issueType, String priority, String description, Long workerId) {
        if (roomNumber == null || roomNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Room number is required");
        }
        String cleanRoomNumber = roomNumber.trim().replaceAll("(?i)^room\\s*", "");
        Room room = roomRepository.findByRoomNumber(cleanRoomNumber)
                .orElseGet(() -> roomRepository.findAll().stream()
                        .filter(r -> r.getRoomNumber().equalsIgnoreCase(cleanRoomNumber))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Room #" + roomNumber + " not found")));

        RoomStatus oldStatus = room.getStatus();
        room.setStatus(RoomStatus.MAINTENANCE);
        roomRepository.save(room);

        Worker worker = workerId != null ? workerRepository.findById(workerId).orElse(null) : null;
        logService.logRoomStatusChange(room, oldStatus, RoomStatus.MAINTENANCE, worker);

        Task task = new Task();
        task.setRoom(room);
        task.setAssignedTo(worker);
        task.setAssignedBy(worker);
        task.setAction("Report Issue: " + (issueType != null ? issueType : "Problem") + " - " + (description != null ? description : "Room maintenance needed"));
        task.setStatus(TaskStatus.ASSIGNED);
        task.setTimestamp(java.time.LocalDateTime.now());
        taskRepository.save(task);

        logService.log(ObjectType.ROOM, ActionCode.UPDATE, room.getId(),
                "Housekeeper reported issue for Room #" + room.getRoomNumber() + ": " + description, workerId);
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
        dto.setProofImageUrl(task.getProofImageUrl());
        return dto;
    }

    private String resolveStatusClass(TaskStatus status) {
        if (status == null) return "status-warning";
        switch (status) {
            case CLEANING: 
                return "status-info";
            case WAITING_INSPECTION:
                return "status-warning";
            case INSPECTED: 
                return "status-success";
            case ASSIGNED: 
                return "status-info";
            case PENDING:
            default: return "status-warning";
        }
    }
}
