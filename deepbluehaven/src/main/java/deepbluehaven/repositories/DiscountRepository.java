package deepbluehaven.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import deepbluehaven.pojo.Discount;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, Long> {
    List<Discount> findByIsActiveTrue();
    Optional<Discount> findByCode(String code);
}
