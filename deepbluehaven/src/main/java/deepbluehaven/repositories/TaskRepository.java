package deepbluehaven.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import deepbluehaven.pojo.Task;
import deepbluehaven.pojo.enums.TaskStatus;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByAssignedToIdOrderByTimestampDesc(Long workerId);
    List<Task> findByAssignedToIdAndStatusOrderByTimestampDesc(Long workerId, TaskStatus status);
    long countByAssignedToIdAndStatus(Long workerId, TaskStatus status);
}
