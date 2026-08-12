package deepbluehaven.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import deepbluehaven.pojo.TaskType;

@Repository
public interface TaskTypeRepository extends JpaRepository<TaskType, Long> {
}
