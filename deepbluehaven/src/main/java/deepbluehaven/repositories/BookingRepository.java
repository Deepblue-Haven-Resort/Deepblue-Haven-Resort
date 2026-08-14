package deepbluehaven.repositories;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import deepbluehaven.pojo.Booking;
import deepbluehaven.pojo.enums.BookingStatus;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    List<Booking> findByCustomerId(Long customerId);

    @Query("""
        SELECT DISTINCT b FROM Booking b LEFT JOIN FETCH b.details d LEFT JOIN FETCH d.room r LEFT JOIN FETCH b.customer c LEFT JOIN FETCH c.profile p WHERE b.customer.id = :customerId ORDER BY b.bookingTime DESC
    """)
    List<Booking> findByCustomerIdWithDetailsAndRoom(@Param("customerId") Long customerId);

    @Query("""
        SELECT DISTINCT b FROM Booking b LEFT JOIN FETCH b.details d LEFT JOIN FETCH d.room r LEFT JOIN FETCH b.customer c LEFT JOIN FETCH c.profile p ORDER BY b.bookingTime DESC
    """)
    List<Booking> findAllWithDetailsAndRoom();

    @Query("""
        SELECT COUNT(bd) > 0 FROM BookingDetail bd WHERE bd.room.id = :roomId AND bd.status IN :activeStatuses AND bd.checkIn < :checkOut AND bd.checkOut > :checkIn
    """)
    boolean existsOverlappingBooking(
        @Param("roomId") Long roomId,
        @Param("checkIn") LocalDate checkIn,
        @Param("checkOut") LocalDate checkOut,
        @Param("activeStatuses") List<BookingStatus> activeStatuses
    );
}


