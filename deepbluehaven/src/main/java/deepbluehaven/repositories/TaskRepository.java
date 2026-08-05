package deepbluehaven.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import deepbluehaven.pojo.Task;
import deepbluehaven.pojo.enums.TaskStatus;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    Long countByStatus(TaskStatus status);

    List<Task> findByAssignedToId(Long workerId);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignedTo.id = :workerId AND t.status = :status")
    Long countByAssignedToIdAndStatus(@Param("workerId") Long workerId, @Param("status") TaskStatus status);

    @Query("SELECT t FROM Task t WHERE t.status != 'COMPLETED' AND t.dueTime IS NOT NULL AND t.dueTime < :now ORDER BY t.dueTime ASC")
    List<Task> findDelayedTasks(@Param("now") LocalDateTime now);

    List<Task> findTop5ByOrderByTimestampDesc();
    List<Task> findByAssignedToIdOrderByTimestampDesc(Long workerId);
    List<Task> findByAssignedToIdAndStatusOrderByTimestampDesc(Long workerId, TaskStatus status);
    long countByAssignedToIdAndStatus(Long workerId, TaskStatus status);
}
