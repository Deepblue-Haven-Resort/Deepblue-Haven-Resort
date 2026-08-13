package deepbluehaven.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import deepbluehaven.pojo.BookingLog;

@Repository
public interface BookingLogRepository extends JpaRepository<BookingLog, Long> {

    List<BookingLog> findTop10ByOrderByTimestampDesc();
    List<BookingLog> findAllByOrderByTimestampDesc();
    List<BookingLog> findByBookingIdOrderByTimestampDesc(Long bookingId);
}
