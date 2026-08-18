package deepbluehaven.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import deepbluehaven.dto.NotificationDTO;
import deepbluehaven.pojo.Customer;
import deepbluehaven.pojo.Notification;
import deepbluehaven.pojo.Worker;
import deepbluehaven.pojo.enums.NotificationType;
import deepbluehaven.pojo.enums.Role;
import deepbluehaven.repositories.NotificationRepository;
import deepbluehaven.repositories.WorkerRepository;

@Service
public class NotificationService {

    private final JavaMailSender mailSender;
    private final NotificationRepository notificationRepository;
    private final WorkerRepository workerRepository;

    @Value("${esms.api.key:}")
    private String apiKey;

    @Value("${esms.api.secret:}")
    private String secretKey;

    @Value("${esms.api.brandname:}")
    private String brandname;

    public NotificationService(JavaMailSender mailSender, 
                               NotificationRepository notificationRepository,
                               WorkerRepository workerRepository) {
        this.mailSender = mailSender;
        this.notificationRepository = notificationRepository;
        this.workerRepository = workerRepository;
    }

    @Transactional
    public void notifyManagers(String title, String message, NotificationType type, String link) {
        List<Worker> managers = workerRepository.findAllWithProfile().stream()
                .filter(w -> w.getProfile() != null && (w.getProfile().getRole() == Role.MANAGER || w.getProfile().getRole() == Role.ADMIN))
                .toList();
        for (Worker m : managers) {
            createWorkerNotification(m, title, message, type, link);
        }
    }

    public void sendEmailOtp(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Deep Blue Haven - OTP Verification");
        message.setText("Your OTP code is: " + otp + ".\nThis code will expire in 3 minutes.");
        mailSender.send(message);
    }

    @Transactional
    public Notification createCustomerNotification(Customer customer, String title, String message, NotificationType type, String link) {
        Notification notification = new Notification(customer, title, message, type);
        notification.setLink(link);
        return notificationRepository.save(notification);
    }

    @Transactional
    public Notification createWorkerNotification(Worker worker, String title, String message, NotificationType type, String link) {
        Notification notification = new Notification(worker, title, message, type);
        notification.setLink(link);
        return notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public NotificationDTO.ListResponse getNotificationsForCustomer(Long customerId) {
        List<Notification> notifications = notificationRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
        List<NotificationDTO.Response> dtos = notifications.stream()
                .map(NotificationDTO.Response::fromEntity)
                .collect(Collectors.toList());
        long unreadCount = notificationRepository.countByCustomerIdAndIsReadFalse(customerId);
        return new NotificationDTO.ListResponse(dtos, unreadCount);
    }

    @Transactional(readOnly = true)
    public NotificationDTO.ListResponse getNotificationsForWorker(Long workerId) {
        List<Notification> notifications = notificationRepository.findByWorkerIdOrderByCreatedAtDesc(workerId);
        List<NotificationDTO.Response> dtos = notifications.stream()
                .map(NotificationDTO.Response::fromEntity)
                .collect(Collectors.toList());
        long unreadCount = notificationRepository.countByWorkerIdAndIsReadFalse(workerId);
        return new NotificationDTO.ListResponse(dtos, unreadCount);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            if (!Boolean.TRUE.equals(notification.getIsRead())) {
                notification.setIsRead(true);
                notification.setReadAt(LocalDateTime.now());
                notificationRepository.save(notification);
            }
        });
    }

    @Transactional
    public void markAllAsReadForCustomer(Long customerId) {
        List<Notification> unread = notificationRepository.findByCustomerIdAndIsReadFalseOrderByCreatedAtDesc(customerId);
        LocalDateTime now = LocalDateTime.now();
        unread.forEach(notification -> {
            notification.setIsRead(true);
            notification.setReadAt(now);
        });
        notificationRepository.saveAll(unread);
    }

    @Transactional
    public void markAllAsReadForWorker(Long workerId) {
        List<Notification> unread = notificationRepository.findByWorkerIdAndIsReadFalseOrderByCreatedAtDesc(workerId);
        LocalDateTime now = LocalDateTime.now();
        unread.forEach(notification -> {
            notification.setIsRead(true);
            notification.setReadAt(now);
        });
        notificationRepository.saveAll(unread);
    }
}