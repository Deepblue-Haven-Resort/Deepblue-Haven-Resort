package deepbluehaven.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import deepbluehaven.pojo.MembershipTier;

@Repository
public interface MembershipTierRepository extends JpaRepository<MembershipTier, Long> {

    @Query("SELECT t FROM MembershipTier t ORDER BY t.minSpent ASC, t.minPoints ASC")
    List<MembershipTier> findAllOrderedForProgression();
}
