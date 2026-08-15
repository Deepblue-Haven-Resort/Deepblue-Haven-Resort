package deepbluehaven.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import deepbluehaven.pojo.Customer;

public interface CustomerRepository
        extends JpaRepository<Customer, Long> {

    @Query("""
                SELECT c FROM Customer c LEFT JOIN FETCH c.profile WHERE c.username = :username
            """)
    Optional<Customer> findByUsername(
            @Param("username") String username);

    @Query("""
                SELECT CASE
                    WHEN COUNT(c) > 0 THEN true
                    ELSE false
                END
                FROM Customer c JOIN c.profile p WHERE LOWER(p.email) = LOWER(:identity) OR p.phoneNumber = :identity
            """)
    boolean existsByIdentity(
            @Param("identity") String identity);

    @Query("""
                SELECT c FROM Customer c JOIN FETCH c.profile p WHERE LOWER(p.email) = LOWER(:identity) OR p.phoneNumber = :identity
            """)
    Optional<Customer> findByIdentity(
            @Param("identity") String identity);

    boolean existsByUsername(String username);

    @Query("""
        SELECT DISTINCT c FROM Customer c LEFT JOIN FETCH c.favoriteRooms r LEFT JOIN FETCH r.resort WHERE c.id = :id
    """)
    Optional<Customer> findByIdWithFavoriteRooms(@Param("id") Long id);

    @Query("""
        SELECT DISTINCT c FROM Customer c LEFT JOIN FETCH c.favoriteServices s LEFT JOIN FETCH s.resort WHERE c.id = :id
    """)
    Optional<Customer> findByIdWithFavoriteServices(@Param("id") Long id);
}