package deepbluehaven.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import deepbluehaven.pojo.Worker;
import deepbluehaven.pojo.enums.Role;
import deepbluehaven.pojo.enums.WorkerStatus;
import io.lettuce.core.dynamic.annotation.Param;

public interface WorkerRepository extends JpaRepository<Worker, Long> {

        @Query("SELECT w FROM Worker w JOIN FETCH w.profile WHERE w.username = :username")

        boolean existsByUsername(String username);

        boolean existsByEmployeeCodeIgnoreCase(String employeeCode);

        boolean existsByProfile_EmailIgnoreCase(String email);

        @EntityGraph(attributePaths = {
                        "profile",
                        "roleTags"
        })
        Optional<Worker> findWithPermissionsById(Long id);

        @EntityGraph(attributePaths = {
                        "profile",
                        "roleTags"
        })
        Optional<Worker> findByUsername(String username);

        boolean existsByUsernameIgnoreCase(String username);

        Optional<Worker> findWithProfileAndPermissionsById(Long workerId);

        
        long countByStatus(WorkerStatus status);

        long countByStatusNot(WorkerStatus status);
        
        @Query("SELECT COUNT(w) FROM Worker w WHERE w.profile.role <> Role.ADMIN " + "AND w.status <> WorkerStatus.INACTIVE")
        long countNonAdmin();
        
        long countByProfile_Role(Role role);

        @Query("""
                SELECT w FROM Worker w JOIN FETCH w.profile p WHERE w.status <> WorkerStatus.INACTIVE
                AND (:search IS NULL OR LOWER(w.username) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.fullName) LIKE LOWER(CONCAT('%', :search, '%')))
                AND (:role IS NULL OR p.role = :role)
                AND (:status IS NULL OR w.status = :status)
                """)
        Page<Worker> search(@Param("search") String search, @Param("role") Role role, @Param("status") WorkerStatus status, Pageable pageable);
}