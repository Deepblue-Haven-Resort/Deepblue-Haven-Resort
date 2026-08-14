package deepbluehaven.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import deepbluehaven.pojo.CustomerProfile;

import java.util.Optional;

public interface CustomerProfileRepository extends JpaRepository<CustomerProfile, Long> {
    Optional<CustomerProfile> findByCustomerId(Long customerId);
}
