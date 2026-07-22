package deepbluehaven.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import deepbluehaven.pojo.Worker;

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
}