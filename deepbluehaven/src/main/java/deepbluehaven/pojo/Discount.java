package deepbluehaven.pojo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import deepbluehaven.pojo.enums.DiscountType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "Discount")
public class Discount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private DiscountType type;

    @Column(name = "discount_value", nullable = false, precision = 15, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "description", length = 500, columnDefinition = "NVARCHAR(500)")
    private String description;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "min_value_service", nullable = true, precision = 15, scale = 2)
    private BigDecimal minValueService;

    @Column(name = "usage_limit", nullable = false)
    private Integer usageLimit;

    @Column(name = "usage_count", nullable = false)
    private Integer usageCount = 0;

    @Column(name = "limit_per_user", nullable = false)
    private Integer limitPerUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "membership_tier_id", nullable = true)
    private MembershipTier membershipTier;

    @Column(name = "room_type", nullable = true, length = 100)
    private String roomType;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "is_stackable", nullable = false)
    private Boolean isStackable = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private Worker createdBy;

    @OneToMany(mappedBy = "discount", fetch = FetchType.LAZY)
    private List<CustomerDiscount> customerDiscounts = new ArrayList<>();

    public Discount() {}

    public Long getId() { 
        return id; }
    public void setId(Long id) { 
        this.id = id; }

    public String getCode() { 
        return code; }
    public void setCode(String code) { 
        this.code = code; }

    public DiscountType getType() { 
        return type; }
    public void setType(DiscountType type) { 
        this.type = type; }

    public BigDecimal getDiscountValue() { 
        return discountValue; }
    public void setDiscountValue(BigDecimal discountValue) { 
        this.discountValue = discountValue; }

    public String getDescription() { return description; }
    public void setDescription(String description) { 
        this.description = description; }

    public LocalDate getStartDate() { 
        return startDate; }
    public void setStartDate(LocalDate startDate) { 
        this.startDate = startDate; }

    public LocalDate getEndDate() { 
        return endDate; }
    public void setEndDate(LocalDate endDate) { 
        this.endDate = endDate; }

    public BigDecimal getMinValueService() { 
        return minValueService; }
    public void setMinValueService(BigDecimal minValueService) { 
        this.minValueService = minValueService; }

    public Integer getUsageLimit() { 
        return usageLimit; }
    public void setUsageLimit(Integer usageLimit) { 
        this.usageLimit = usageLimit; }

    public Integer getUsageCount() { 
        return usageCount; }
    public void setUsageCount(Integer usageCount) { 
        this.usageCount = usageCount; }

    public Integer getLimitPerUser() { 
        return limitPerUser; }
    public void setLimitPerUser(Integer limitPerUser) { 
        this.limitPerUser = limitPerUser; }

    public MembershipTier getMembershipTier() { 
        return membershipTier; }
    public void setMembershipTier(MembershipTier membershipTier) { 
        this.membershipTier = membershipTier; }

    public String getRoomType() { 
        return roomType; }
    public void setRoomType(String roomType) { 
        this.roomType = roomType; }

    public Boolean getIsActive() { 
        return isActive; }
    public void setIsActive(Boolean isActive) { 
        this.isActive = isActive; }

    public Boolean getIsStackable() { 
        return isStackable; }
    public void setIsStackable(Boolean isStackable) { 
        this.isStackable = isStackable; }

    public LocalDateTime getCreatedAt() { 
        return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { 
        this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { 
        return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { 
        this.updatedAt = updatedAt; }

    public Worker getCreatedBy() { 
        return createdBy; }
    public void setCreatedBy(Worker createdBy) { 
        this.createdBy = createdBy; }

    public List<CustomerDiscount> getCustomerDiscounts() { 
        return customerDiscounts; }
    public void setCustomerDiscounts(List<CustomerDiscount> customerDiscounts) { 
        this.customerDiscounts = customerDiscounts; }
}
