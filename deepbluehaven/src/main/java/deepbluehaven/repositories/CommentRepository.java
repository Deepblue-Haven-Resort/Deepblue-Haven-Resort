package deepbluehaven.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import deepbluehaven.pojo.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT AVG(c.rating) FROM Comment c")
    Double getAverageRating();

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.isComplaint = true OR c.rating <= 2")
    Long countComplaints();

    List<Comment> findTop5ByOrderByCreatedAtDesc();
}
