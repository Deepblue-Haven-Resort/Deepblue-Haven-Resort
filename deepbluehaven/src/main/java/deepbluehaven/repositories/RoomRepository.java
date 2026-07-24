package deepbluehaven.repositories;
 
import java.util.List;
 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
 
import deepbluehaven.pojo.Room;
import deepbluehaven.pojo.enums.RoomStatus;
import io.lettuce.core.dynamic.annotation.Param;
 
public interface RoomRepository extends JpaRepository<Room, Long> {
    @Query("SELECT r FROM Room r JOIN FETCH r.resort WHERE r.status IN :statuses ORDER BY r.id")
    List<Room> findByStatusesWithResort(@Param("statuses") List<RoomStatus> statuses);
}
 