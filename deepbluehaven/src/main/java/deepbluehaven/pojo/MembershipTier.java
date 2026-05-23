package deepbluehaven.pojo;

import deepbluehaven.pojo.enums.TierStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Membership_Tier")
public class MembershipTier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tier_name", nullable = false, unique = true, length = 50)
    private TierStatus tierName;

    @Column(name = "min_points", nullable = false)
    private Integer minPoints;

    @Column(name = "point_multiplier", nullable = false, precision = 5, scale = 2)
    private BigDecimal pointMultiplier;

    @Column(name = "discount_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal discountRate;

    @Column(name = "priority_duration", nullable = false)
    private Integer priorityDuration;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "membershipTier", fetch = FetchType.LAZY)
    private List<CustomerProfile> customerProfiles = new ArrayList<>();

    @OneToMany(mappedBy = "membershipTier", fetch = FetchType.LAZY)
    private List<Discount> discounts = new ArrayList<>();

    public MembershipTier() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public TierStatus getTierName() { 
        return tierName; }
    public void setTierName(TierStatus tierName) { 
        this.tierName = tierName; }

    public Integer getMinPoints() { 
        return minPoints; }
    public void setMinPoints(Integer minPoints) { 
        this.minPoints = minPoints; }

    public BigDecimal getPointMultiplier() { 
        return pointMultiplier; }
    public void setPointMultiplier(BigDecimal pointMultiplier) { 
        this.pointMultiplier = pointMultiplier; }

    public BigDecimal getDiscountRate() { 
        return discountRate; }
    public void setDiscountRate(BigDecimal discountRate) { 
        this.discountRate = discountRate; }

    public Integer getPriorityDuration() { 
        return priorityDuration; }
    public void setPriorityDuration(Integer priorityDuration) { 
        this.priorityDuration = priorityDuration; }

    public String getDescription() { 
        return description; }
    public void setDescription(String description) { 
        this.description = description; }

    public List<CustomerProfile> getCustomerProfiles() { 
        return customerProfiles; }
    public void setCustomerProfiles(List<CustomerProfile> customerProfiles) { 
        this.customerProfiles = customerProfiles; }

    public List<Discount> getDiscounts() { 
        return discounts; }
    public void setDiscounts(List<Discount> discounts) { 
        this.discounts = discounts; }
}
