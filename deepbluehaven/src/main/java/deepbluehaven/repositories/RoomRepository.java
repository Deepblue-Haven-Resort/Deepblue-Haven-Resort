package deepbluehaven.repositories;
 
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import deepbluehaven.pojo.Room;
import deepbluehaven.pojo.enums.RoomStatus;
import jakarta.persistence.LockModeType;

public interface RoomRepository extends JpaRepository<Room, Long> {
    @Query("SELECT r FROM Room r JOIN FETCH r.resort WHERE r.status IN :statuses ORDER BY r.id")
    List<Room> findByStatusesWithResort(@Param("statuses") List<RoomStatus> statuses);

    List<Room> findByStatus(RoomStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Room r WHERE r.id = :id")
    Optional<Room> findByIdWithPessimisticLock(@Param("id") Long id);
}


 