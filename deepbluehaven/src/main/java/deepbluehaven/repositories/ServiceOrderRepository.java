package deepbluehaven.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import deepbluehaven.pojo.ServiceOrder;

@Repository
public interface ServiceOrderRepository extends JpaRepository<ServiceOrder, Long> {
    List<ServiceOrder> findByBookingId(Long bookingId);
    List<ServiceOrder> findByCustomerId(Long customerId);

    @Query("SELECT so FROM ServiceOrder so LEFT JOIN FETCH so.service LEFT JOIN FETCH so.booking LEFT JOIN FETCH so.customer LEFT JOIN FETCH so.processedBy ORDER BY so.orderTime DESC")
    List<ServiceOrder> findAllWithDetailsOrderByOrderTimeDesc();

    @Query("SELECT so FROM ServiceOrder so LEFT JOIN FETCH so.service LEFT JOIN FETCH so.booking LEFT JOIN FETCH so.customer LEFT JOIN FETCH so.processedBy WHERE so.status = :status ORDER BY so.orderTime DESC")
    List<ServiceOrder> findByStatusWithDetailsOrderByOrderTimeDesc(@Param("status") deepbluehaven.pojo.enums.ServiceOrderStatus status);
}
