package deepbluehaven.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import deepbluehaven.pojo.Notification;
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    List<Notification> findByCustomerIdAndIsReadFalseOrderByCreatedAtDesc(Long customerId);

    List<Notification> findByWorkerIdOrderByCreatedAtDesc(Long workerId);

    List<Notification> findByWorkerIdAndIsReadFalseOrderByCreatedAtDesc(Long workerId);

    long countByCustomerIdAndIsReadFalse(Long customerId);

    long countByWorkerIdAndIsReadFalse(Long workerId);
}
