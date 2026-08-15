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

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.response IS NULL OR TRIM(c.response) = ''")
    Long countUnanswered();

    List<Comment> findTop5ByOrderByCreatedAtDesc();

    List<Comment> findByCustomerId(Long customerId);

    List<Comment> findAllByOrderByCreatedAtDesc();

    List<Comment> findByIsComplaintTrueOrderByCreatedAtDesc();

    List<Comment> findByRatingGreaterThanEqualOrderByCreatedAtDesc(Integer rating);

    List<Comment> findByRatingLessThanEqualOrderByCreatedAtDesc(Integer rating);
}
