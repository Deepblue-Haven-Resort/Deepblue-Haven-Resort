package deepbluehaven.pojo;

import deepbluehaven.pojo.enums.CalculationType;
import deepbluehaven.pojo.enums.ServiceCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "Service_Point")
public class ServicePoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_category", nullable = false, length = 50)
    private ServiceCategory serviceCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "calculation_type", nullable = false, length = 50)
    private CalculationType calculationType;

    @Column(name = "fixed_points", nullable = true)
    private Integer fixedPoints;

    @Column(name = "reward_percentage", nullable = true, precision = 5, scale = 2)
    private BigDecimal rewardPercentage;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    public ServicePoint() {}

    public Long getId() { 
        return id; }
    public void setId(Long id) { 
        this.id = id; }

    public ServiceCategory getServiceCategory() { 
        return serviceCategory; }
    public void setServiceCategory(ServiceCategory serviceCategory) { 
        this.serviceCategory = serviceCategory; }

    public CalculationType getCalculationType() { 
        return calculationType; }
    public void setCalculationType(CalculationType calculationType) { 
        this.calculationType = calculationType; }

    public Integer getFixedPoints() { 
        return fixedPoints; }
    public void setFixedPoints(Integer fixedPoints) { 
        this.fixedPoints = fixedPoints; }

    public BigDecimal getRewardPercentage() { 
        return rewardPercentage; }
    public void setRewardPercentage(BigDecimal rewardPercentage) { 
        this.rewardPercentage = rewardPercentage; }

    public Boolean getIsActive() { 
        return isActive; }
    public void setIsActive(Boolean isActive) { 
        this.isActive = isActive; }
}
