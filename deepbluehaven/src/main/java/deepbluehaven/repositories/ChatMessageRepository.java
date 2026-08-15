package deepbluehaven.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import deepbluehaven.pojo.ChatMessage;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByChatSessionIdOrderByTimestampAsc(Long sessionId);

    @Query("SELECT m FROM ChatMessage m WHERE m.chatSession.id = :sessionId AND m.id > :afterId ORDER BY m.timestamp ASC")
    List<ChatMessage> findNewMessages(@Param("sessionId") Long sessionId, @Param("afterId") Long afterId);

    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.chatSession.id = :sessionId AND m.isRead = false AND m.senderType = 'CUSTOMER'")
    Long countUnreadByStaff(@Param("sessionId") Long sessionId);
}
