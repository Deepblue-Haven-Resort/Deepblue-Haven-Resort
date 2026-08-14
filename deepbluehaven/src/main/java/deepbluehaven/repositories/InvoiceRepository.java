package deepbluehaven.repositories;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import deepbluehaven.pojo.Invoice;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM Invoice i WHERE i.timestamp >= :startTime AND i.timestamp < :endTime")
    BigDecimal sumTotalAmountBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Query("SELECT COALESCE(SUM(i.paidAmount), 0) FROM Invoice i WHERE i.timestamp >= :startTime AND i.timestamp < :endTime")
    BigDecimal sumPaidAmountBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    List<Invoice> findByBookingId(Long bookingId);

    List<Invoice> findByCustomerId(Long customerId);

    @Query("SELECT DISTINCT i FROM Invoice i LEFT JOIN FETCH i.booking b LEFT JOIN FETCH i.customer c LEFT JOIN FETCH c.profile p ORDER BY i.timestamp DESC")
    List<Invoice> findAllWithBookingAndCustomer();
}
