package deepbluehaven.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import deepbluehaven.pojo.ContactInquiry;
import deepbluehaven.pojo.enums.InquiryStatus;

@Repository
public interface ContactInquiryRepository extends JpaRepository<ContactInquiry, Long> {

    List<ContactInquiry> findAllByOrderByCreatedAtDesc();

    List<ContactInquiry> findByStatusOrderByCreatedAtDesc(InquiryStatus status);

    @Query("SELECT COUNT(ci) FROM ContactInquiry ci WHERE ci.status = 'PENDING'")
    Long countPendingInquiries();
}
