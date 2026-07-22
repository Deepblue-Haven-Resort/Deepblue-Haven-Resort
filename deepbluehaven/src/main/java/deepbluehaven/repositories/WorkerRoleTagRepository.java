package deepbluehaven.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import deepbluehaven.pojo.WorkerRoleTag;
import deepbluehaven.pojo.enums.PermissionTag;

public interface WorkerRoleTagRepository
        extends JpaRepository<WorkerRoleTag, Long> {

    boolean existsByWorker_IdAndPermissionTag(
            Long workerId,
            PermissionTag permissionTag
    );
}