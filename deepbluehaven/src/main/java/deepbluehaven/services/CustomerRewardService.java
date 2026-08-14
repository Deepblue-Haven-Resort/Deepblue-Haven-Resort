package deepbluehaven.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import deepbluehaven.pojo.Customer;
import deepbluehaven.pojo.CustomerLoyaltyLog;
import deepbluehaven.pojo.CustomerProfile;
import deepbluehaven.pojo.MembershipTier;
import deepbluehaven.pojo.enums.ReferenceType;
import deepbluehaven.repositories.CustomerLoyaltyLogRepository;
import deepbluehaven.repositories.CustomerProfileRepository;
import deepbluehaven.repositories.MembershipTierRepository;

@Service
public class CustomerRewardService {

    private final CustomerProfileRepository customerProfileRepository;
    private final MembershipTierRepository membershipTierRepository;
    private final CustomerLoyaltyLogRepository customerLoyaltyLogRepository;

    public CustomerRewardService(CustomerProfileRepository customerProfileRepository,
                                 MembershipTierRepository membershipTierRepository,
                                 CustomerLoyaltyLogRepository customerLoyaltyLogRepository) {
        this.customerProfileRepository = customerProfileRepository;
        this.membershipTierRepository = membershipTierRepository;
        this.customerLoyaltyLogRepository = customerLoyaltyLogRepository;
    }

    @Transactional
    public void processInvoicePayment(Customer customer, BigDecimal amountPaid, Long invoiceId, String note) {
        if (customer == null || amountPaid == null || amountPaid.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        CustomerProfile profile = customer.getProfile();
        if (profile == null) {
            profile = customerProfileRepository.findByCustomerId(customer.getId()).orElse(null);
            if (profile == null) return;
        }

        // 1. Calculate points base: 1 point per 10,000 VND
        BigDecimal basePointsDecimal = amountPaid.divide(new BigDecimal("10000"), 2, RoundingMode.DOWN);
        
        // 2. Multiplier from current MembershipTier (default 1.00)
        BigDecimal multiplier = BigDecimal.ONE;
        if (profile.getMembershipTier() != null && profile.getMembershipTier().getPointMultiplier() != null) {
            multiplier = profile.getMembershipTier().getPointMultiplier();
        }
        
        int pointsEarned = basePointsDecimal.multiply(multiplier).setScale(0, RoundingMode.DOWN).intValue();
        if (pointsEarned <= 0) pointsEarned = 1;

        // 3. Update Profile totals
        profile.setTotalSpent((profile.getTotalSpent() != null ? profile.getTotalSpent() : BigDecimal.ZERO).add(amountPaid));
        profile.setTotalPoints((profile.getTotalPoints() != null ? profile.getTotalPoints() : 0) + pointsEarned);
        profile.setTotalBookings((profile.getTotalBookings() != null ? profile.getTotalBookings() : 0) + 1);

        // 4. Dynamic Tier Progression Check against DB rules
        updateTierAndSegment(profile);

        customerProfileRepository.save(profile);

        // 5. Create Loyalty Log Entry
        CustomerLoyaltyLog log = new CustomerLoyaltyLog();
        log.setCustomer(customer);
        log.setReferenceType(ReferenceType.INVOICE);
        log.setReferenceId(invoiceId != null ? invoiceId : 0L);
        log.setPointsChanged(pointsEarned);
        log.setReason((note != null ? note : "Payment Reward") + " (+" + pointsEarned + " pts)");
        customerLoyaltyLogRepository.save(log);
    }

    @Transactional
    public void updateTierAndSegment(CustomerProfile profile) {
        List<MembershipTier> tiers = membershipTierRepository.findAllOrderedForProgression();
        if (tiers.isEmpty()) return;

        BigDecimal totalSpent = profile.getTotalSpent() != null ? profile.getTotalSpent() : BigDecimal.ZERO;
        int totalPoints = profile.getTotalPoints() != null ? profile.getTotalPoints() : 0;

        MembershipTier qualifiedTier = tiers.get(0); // Default Bronze

        for (MembershipTier tier : tiers) {
            boolean spentQualifies = tier.getMinSpent() != null && totalSpent.compareTo(tier.getMinSpent()) >= 0;
            boolean pointsQualifies = tier.getMinPoints() != null && totalPoints >= tier.getMinPoints();

            if (spentQualifies || pointsQualifies) {
                qualifiedTier = tier;
            }
        }

        profile.setMembershipTier(qualifiedTier);
        if (qualifiedTier.getTierName() != null) {
            profile.setSegment(qualifiedTier.getTierName().name());
        }
    }
}
