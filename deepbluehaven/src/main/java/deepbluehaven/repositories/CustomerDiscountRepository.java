package deepbluehaven.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import deepbluehaven.pojo.CustomerDiscount;
import deepbluehaven.pojo.enums.CustomerDiscountStatus;

@Repository
public interface CustomerDiscountRepository extends JpaRepository<CustomerDiscount, Long> {

    List<CustomerDiscount> findByCustomerId(Long customerId);

    List<CustomerDiscount> findByCustomerIdAndStatus(Long customerId, CustomerDiscountStatus status);

    @Query("SELECT cd FROM CustomerDiscount cd WHERE cd.customer.id = :customerId AND cd.discount.id = :discountId")
    Optional<CustomerDiscount> findByCustomerIdAndDiscountId(@Param("customerId") Long customerId, @Param("discountId") Long discountId);

    @Query("SELECT cd FROM CustomerDiscount cd JOIN FETCH cd.discount WHERE cd.customer.id = :customerId AND cd.status = :status")
    List<CustomerDiscount> findByCustomerIdAndStatusWithDiscount(@Param("customerId") Long customerId, @Param("status") CustomerDiscountStatus status);
}
