package deepbluehaven.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import deepbluehaven.pojo.ChatSession;
import deepbluehaven.pojo.enums.ChatStatus;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    List<ChatSession> findByCustomerIdOrderByUpdatedAtDesc(Long customerId);

    @Query("SELECT cs FROM ChatSession cs WHERE cs.customer.id = :customerId AND cs.status != 'RESOLVED' ORDER BY cs.updatedAt DESC")
    List<ChatSession> findActiveSessionsByCustomerId(@Param("customerId") Long customerId);

    Optional<ChatSession> findFirstByCustomerIdAndStatusNotOrderByUpdatedAtDesc(Long customerId, ChatStatus status);

    List<ChatSession> findByStatusOrderByUpdatedAtDesc(ChatStatus status);

    List<ChatSession> findAllByOrderByUpdatedAtDesc();

    @Query("SELECT COUNT(cs) FROM ChatSession cs WHERE cs.status = 'WAITING'")
    Long countWaitingSessions();

    @Query("SELECT COUNT(cs) FROM ChatSession cs WHERE cs.status = 'IN_PROGRESS'")
    Long countInProgressSessions();
}
