package deepbluehaven.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import deepbluehaven.pojo.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @Query("SELECT c FROM Customer c LEFT JOIN FETCH c.profile WHERE c.username = :username")
    Optional<Customer> findByUsername(@Param("username") String username);

    @Query("SELECT COUNT(c) > 0 FROM Customer c JOIN c.profile p WHERE c.username = :identity OR p.email = :identity OR p.phoneNumber = :identity")
    boolean existsByIdentity(@Param("identity") String identity);
    
    @Query("SELECT c FROM Customer c JOIN c.profile p WHERE c.username = :identity OR p.email = :identity OR p.phoneNumber = :identity")
    Optional<Customer> findByIdentity(@Param("identity") String identity);
    
    boolean existsByUsername(String username);
}