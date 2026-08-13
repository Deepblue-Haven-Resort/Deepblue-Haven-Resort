package deepbluehaven.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import deepbluehaven.pojo.AuthAccessLog;
import deepbluehaven.pojo.Booking;
import deepbluehaven.pojo.BookingLog;
import deepbluehaven.pojo.Invoice;
import deepbluehaven.pojo.InvoiceStatusLog;
import deepbluehaven.pojo.Log;
import deepbluehaven.pojo.Room;
import deepbluehaven.pojo.RoomStatusLog;
import deepbluehaven.pojo.Worker;
import deepbluehaven.pojo.enums.ActionCode;
import deepbluehaven.pojo.enums.BookingStatus;
import deepbluehaven.pojo.enums.InvoiceStatus;
import deepbluehaven.pojo.enums.ObjectType;
import deepbluehaven.pojo.enums.RoomStatus;
import deepbluehaven.repositories.AuthAccessLogRepository;
import deepbluehaven.repositories.BookingLogRepository;
import deepbluehaven.repositories.InvoiceStatusLogRepository;
import deepbluehaven.repositories.LogRepository;
import deepbluehaven.repositories.RoomStatusLogRepository;
import jakarta.servlet.http.HttpSession;

@Service
public class LogService {

    private static final Logger logger = LoggerFactory.getLogger(LogService.class);
    private final LogRepository logRepository;
    private final AuthAccessLogRepository authAccessLogRepository;
    private final BookingLogRepository bookingLogRepository;
    private final RoomStatusLogRepository roomStatusLogRepository;
    private final InvoiceStatusLogRepository invoiceStatusLogRepository;
    private final ObjectMapper objectMapper;

    public LogService(LogRepository logRepository,
                      AuthAccessLogRepository authAccessLogRepository,
                      BookingLogRepository bookingLogRepository,
                      RoomStatusLogRepository roomStatusLogRepository,
                      InvoiceStatusLogRepository invoiceStatusLogRepository) {
        this.logRepository = logRepository;
        this.authAccessLogRepository = authAccessLogRepository;
        this.bookingLogRepository = bookingLogRepository;
        this.roomStatusLogRepository = roomStatusLogRepository;
        this.invoiceStatusLogRepository = invoiceStatusLogRepository;
        this.objectMapper = new ObjectMapper();
    }

    public AuthAccessLog logAuthAccess(Long accountId, String accountType, String action, String ipAddress, String userAgent) {
        try {
            AuthAccessLog authLog = new AuthAccessLog();
            authLog.setAccountId(accountId != null ? accountId : 0L);
            authLog.setAccountType(accountType);
            authLog.setAction(action);
            authLog.setIpAddress(ipAddress != null ? ipAddress : "127.0.0.1");
            authLog.setUserAgent(userAgent != null ? userAgent : "Unknown");
            AuthAccessLog saved = authAccessLogRepository.save(authLog);
            logger.info("[AUTH LOG] Account #{} ({}) Action: {}", accountId, accountType, action);
            return saved;
        } catch (Exception e) {
            logger.error("Failed to save auth access log: {}", e.getMessage(), e);
            return null;
        }
    }

    public Log log(ObjectType objectType, ActionCode actionCode, Long objectId, String description, Long workerId) {
        try {
            Log logRecord = new Log();
            logRecord.setObjectType(objectType);
            logRecord.setActionCode(actionCode);
            logRecord.setObjectId(objectId != null ? objectId : 0L);
            logRecord.setWorkerId(workerId);

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("description", description);
            logRecord.setMetadata(objectMapper.writeValueAsString(metadata));

            Log savedLog = logRepository.save(logRecord);
            logger.info("[AUDIT LOG] Action: {} | Object: {} (ID: {}) | WorkerId: {}", 
                    actionCode, objectType, objectId, workerId);
            return savedLog;
        } catch (Exception e) {
            logger.error("Failed to save audit log: {}", e.getMessage(), e);
            return null;
        }
    }

    public Log log(ObjectType objectType, ActionCode actionCode, Long objectId, String description) {
        return log(objectType, actionCode, objectId, description, (Long) null);
    }

    public Log log(ObjectType objectType, ActionCode actionCode, Long objectId, String description, HttpSession session) {
        Long workerId = extractWorkerId(session);
        return log(objectType, actionCode, objectId, description, workerId);
    }

    public Log logWithStateDiff(ObjectType objectType, ActionCode actionCode, Long objectId, 
                               String description, Object previousState, Object currentState, HttpSession session) {
        try {
            Long workerId = extractWorkerId(session);
            Log logRecord = new Log();
            logRecord.setObjectType(objectType);
            logRecord.setActionCode(actionCode);
            logRecord.setObjectId(objectId != null ? objectId : 0L);
            logRecord.setWorkerId(workerId);

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("description", description);
            if (previousState != null) metadata.put("previousState", previousState);
            if (currentState != null) metadata.put("currentState", currentState);

            logRecord.setMetadata(objectMapper.writeValueAsString(metadata));

            Log savedLog = logRepository.save(logRecord);
            logger.info("[AUDIT LOG DIFF] Action: {} | Object: {} (ID: {}) | WorkerId: {}", 
                    actionCode, objectType, objectId, workerId);
            return savedLog;
        } catch (Exception e) {
            logger.error("Failed to save audit log with state diff: {}", e.getMessage(), e);
            return null;
        }
    }

    public List<AuthAccessLog> getRecentAdminAuthAccessLogs() {
        return authAccessLogRepository.findTop10ByOrderByTimestampDesc();
    }

    public List<AuthAccessLog> getAdminAuthAccessLogs() {
        return authAccessLogRepository.findAllByOrderByTimestampDesc();
    }

    public long getFailedLoginCount() {
        return authAccessLogRepository.countByAction("LOGIN_FAILED");
    }

    public long getActiveSessionCount() {
        java.time.LocalDateTime eightHoursAgo = java.time.LocalDateTime.now().minusHours(8);
        return authAccessLogRepository.countActiveSessionsSince(eightHoursAgo);
    }

    public RoomStatusLog logRoomStatusChange(Room room, RoomStatus prevStatus, RoomStatus newStatus, Worker worker) {
        try {
            RoomStatusLog rLog = new RoomStatusLog();
            rLog.setRoom(room);
            rLog.setWorker(worker);
            rLog.setPreviousStatus(prevStatus);
            rLog.setCurrentStatus(newStatus);
            RoomStatusLog saved = roomStatusLogRepository.save(rLog);
            log(ObjectType.ROOM, ActionCode.UPDATE, room != null ? room.getId() : 0L,
                "Room #" + (room != null ? room.getRoomNumber() : "N/A") + " status changed to " + newStatus,
                worker != null ? worker.getId() : null);
            return saved;
        } catch (Exception e) {
            logger.error("Failed to save room status log: {}", e.getMessage(), e);
            return null;
        }
    }

    public BookingLog logBookingStatusChange(Booking booking, BookingStatus prevStatus, BookingStatus newStatus, Long actorId, String note) {
        try {
            BookingLog bLog = new BookingLog();
            bLog.setBooking(booking);
            bLog.setActorId(actorId != null ? actorId : 0L);
            bLog.setPreviousStatus(prevStatus);
            bLog.setCurrentStatus(newStatus);
            bLog.setNote(note != null ? note : "Booking status updated to " + newStatus);
            BookingLog saved = bookingLogRepository.save(bLog);
            log(ObjectType.BOOKING, ActionCode.UPDATE, booking != null ? booking.getId() : 0L,
                "Booking #" + (booking != null ? booking.getId() : 0L) + " status changed to " + newStatus, actorId);
            return saved;
        } catch (Exception e) {
            logger.error("Failed to save booking log: {}", e.getMessage(), e);
            return null;
        }
    }

    public InvoiceStatusLog logInvoiceStatusChange(Invoice invoice, InvoiceStatus prevStatus, InvoiceStatus newStatus, Worker worker) {
        try {
            InvoiceStatusLog iLog = new InvoiceStatusLog();
            iLog.setInvoice(invoice);
            iLog.setWorker(worker);
            iLog.setPreviousStatus(prevStatus);
            iLog.setCurrentStatus(newStatus);
            InvoiceStatusLog saved = invoiceStatusLogRepository.save(iLog);
            log(ObjectType.INVOICE, ActionCode.UPDATE, invoice != null ? invoice.getId() : 0L,
                "Invoice #" + (invoice != null ? invoice.getId() : 0L) + " status changed to " + newStatus,
                worker != null ? worker.getId() : null);
            return saved;
        } catch (Exception e) {
            logger.error("Failed to save invoice status log: {}", e.getMessage(), e);
            return null;
        }
    }

    public List<Log> getRecentAdminSystemLogs() {
        return logRepository.findTop10ByObjectTypeInOrderByTimestampDesc(
                List.of(ObjectType.WORKER, ObjectType.USER, ObjectType.SYSTEM)
        );
    }

    public List<Log> getAdminSystemLogs() {
        return logRepository.findByObjectTypeInOrderByTimestampDesc(
                List.of(ObjectType.WORKER, ObjectType.USER, ObjectType.SYSTEM)
        );
    }

    public List<BookingLog> getRecentManagerBookingLogs() {
        return bookingLogRepository.findTop10ByOrderByTimestampDesc();
    }

    public List<BookingLog> getManagerBookingLogs() {
        return bookingLogRepository.findAllByOrderByTimestampDesc();
    }

    public List<deepbluehaven.pojo.RoomStatusLog> getManagerRoomStatusLogs() {
        return roomStatusLogRepository.findAllByOrderByTimestampDesc();
    }

    public List<deepbluehaven.pojo.InvoiceStatusLog> getManagerInvoiceStatusLogs() {
        return invoiceStatusLogRepository.findAllByOrderByTimestampDesc();
    }

    public List<Log> getRecentManagerOperationalLogs() {
        return logRepository.findTop10ByObjectTypeInOrderByTimestampDesc(List.of(ObjectType.BOOKING, ObjectType.ROOM, ObjectType.SERVICE, ObjectType.PRICING, ObjectType.DISCOUNT, ObjectType.PAYMENT, ObjectType.TASK, ObjectType.INVENTORY)
        );
    }

    public List<Log> getManagerOperationalLogs() {
        return logRepository.findByObjectTypeInOrderByTimestampDesc(
                List.of(ObjectType.BOOKING, ObjectType.ROOM, ObjectType.SERVICE, ObjectType.PRICING, ObjectType.DISCOUNT, ObjectType.PAYMENT, ObjectType.TASK, ObjectType.INVENTORY)
        );
    }

    private Long extractWorkerId(HttpSession session) {
        if (session != null && session.getAttribute("loggedInWorkerId") != null) {
            Object workerIdObj = session.getAttribute("loggedInWorkerId");
            if (workerIdObj instanceof Long longId) {
                return longId;
            } else if (workerIdObj instanceof Number num) {
                return num.longValue();
            } else if (workerIdObj instanceof String str) {
                try {
                    return Long.parseLong(str);
                } catch (NumberFormatException ignored) {}
            }
        }
        return null;
    }
}
