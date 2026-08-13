package deepbluehaven.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import deepbluehaven.pojo.WorkerRoomAssignmentLog;

@Repository
public interface WorkerRoomAssignmentLogRepository extends JpaRepository<WorkerRoomAssignmentLog, Long> {

    List<WorkerRoomAssignmentLog> findTop10ByOrderByTimestampDesc();
    List<WorkerRoomAssignmentLog> findAllByOrderByTimestampDesc();
    List<WorkerRoomAssignmentLog> findByWorkerIdOrderByTimestampDesc(Long workerId);
}
