package deepbluehaven.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import deepbluehaven.pojo.PricingRule;

@Repository
public interface PricingRuleRepository extends JpaRepository<PricingRule, Long> {
}
