package deepbluehaven.services;

import org.springframework.stereotype.Service;

import deepbluehaven.pojo.enums.PermissionTag;
import deepbluehaven.repositories.WorkerRoleTagRepository;

@Service
public class PermissionService {

    private final WorkerRoleTagRepository roleTagRepository;

    public PermissionService(
            WorkerRoleTagRepository roleTagRepository) {

        this.roleTagRepository = roleTagRepository;
    }

    public boolean hasPermission(
            Long workerId,
            PermissionTag permission) {

        if (workerId == null || permission == null) {
            return false;
        }

        return roleTagRepository
                .existsByWorker_IdAndPermissionTag(
                        workerId,
                        permission);
    }
}