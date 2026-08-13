package deepbluehaven.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import deepbluehaven.pojo.RoomStatusLog;

@Repository
public interface RoomStatusLogRepository extends JpaRepository<RoomStatusLog, Long> {

    List<RoomStatusLog> findTop10ByOrderByTimestampDesc();
    List<RoomStatusLog> findAllByOrderByTimestampDesc();
    List<RoomStatusLog> findByRoomIdOrderByTimestampDesc(Long roomId);
}
