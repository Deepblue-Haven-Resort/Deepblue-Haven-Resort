package deepbluehaven.pojo;

import deepbluehaven.pojo.enums.ServiceCategory;
import deepbluehaven.pojo.enums.ServiceStatus;
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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Service")
public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resort_id", nullable = false)
    private Resort resort;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "type", nullable = false, length = 100)
    private String type;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private ServiceCategory category;

    @Column(name = "base_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "unit", nullable = false, length = 50)
    private String unit;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ServiceStatus status;

    @OneToMany(mappedBy = "service", fetch = FetchType.LAZY)
    private List<ServiceOrder> orders = new ArrayList<>();

    public Service() {}

    public Long getId() { 
        return id; }
    public void setId(Long id) { 
        this.id = id; }

    public Resort getResort() { 
        return resort; }
    public void setResort(Resort resort) { 
        this.resort = resort; }

    public String getName() { 
        return name; }
    public void setName(String name) { 
        this.name = name; }

    public String getDescription() { 
        return description; }
    public void setDescription(String description) { 
        this.description = description; }

    public String getType() { 
        return type; }
    public void setType(String type) { 
        this.type = type; }

    public ServiceCategory getCategory() { 
        return category; }
    public void setCategory(ServiceCategory category) { 
        this.category = category; }

    public BigDecimal getBasePrice() { 
        return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { 
        this.basePrice = basePrice; }

    public String getUnit() { 
        return unit; }
    public void setUnit(String unit) { 
        this.unit = unit; }

    public ServiceStatus getStatus() { 
        return status; }
    public void setStatus(ServiceStatus status) { 
        this.status = status; }

    public List<ServiceOrder> getOrders() { 
        return orders; }
    public void setOrders(List<ServiceOrder> orders) { 
        this.orders = orders; }
}
