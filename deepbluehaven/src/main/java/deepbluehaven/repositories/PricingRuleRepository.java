package deepbluehaven.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import deepbluehaven.pojo.PricingRule;
import deepbluehaven.pojo.enums.RoomType;

@Repository
public interface PricingRuleRepository extends JpaRepository<PricingRule, Long> {
    List<PricingRule> findByRoomType(RoomType roomType);
}
