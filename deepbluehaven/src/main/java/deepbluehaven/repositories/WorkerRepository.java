package deepbluehaven.repositories;

import deepbluehaven.pojo.Worker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface WorkerRepository extends JpaRepository<Worker, Long> {

    @Query("SELECT w FROM Worker w JOIN FETCH w.profile WHERE w.username = :username")
    Optional<Worker> findByUsername(@Param("username") String username);

    boolean existsByUsername(String username);
}