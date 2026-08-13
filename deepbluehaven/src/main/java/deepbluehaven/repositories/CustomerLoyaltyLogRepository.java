package deepbluehaven.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import deepbluehaven.pojo.CustomerLoyaltyLog;

@Repository
public interface CustomerLoyaltyLogRepository extends JpaRepository<CustomerLoyaltyLog, Long> {

    List<CustomerLoyaltyLog> findTop10ByOrderByTimestampDesc();
    List<CustomerLoyaltyLog> findAllByOrderByTimestampDesc();
    List<CustomerLoyaltyLog> findByCustomerIdOrderByTimestampDesc(Long customerId);
}
