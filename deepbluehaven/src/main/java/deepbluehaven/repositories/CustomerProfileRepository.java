package deepbluehaven.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import deepbluehaven.pojo.CustomerProfile;

public interface CustomerProfileRepository extends JpaRepository<CustomerProfile, Long> {
}
