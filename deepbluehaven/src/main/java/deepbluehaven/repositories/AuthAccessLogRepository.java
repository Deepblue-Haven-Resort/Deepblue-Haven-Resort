package deepbluehaven.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import deepbluehaven.pojo.AuthAccessLog;

@Repository
public interface AuthAccessLogRepository extends JpaRepository<AuthAccessLog, Long> {

    List<AuthAccessLog> findTop10ByOrderByTimestampDesc();
    List<AuthAccessLog> findAllByOrderByTimestampDesc();
    long countByAction(String action);

    @Query("SELECT COUNT(DISTINCT a.accountId) FROM AuthAccessLog a WHERE a.action = 'LOGIN_SUCCESS' AND a.timestamp >= :since")
    long countActiveSessionsSince(@Param("since") java.time.LocalDateTime since);
}
