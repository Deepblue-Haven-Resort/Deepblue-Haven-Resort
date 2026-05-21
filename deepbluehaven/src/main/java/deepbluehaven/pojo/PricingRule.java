package deepbluehaven.pojo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "Pricing_Rule")
public class PricingRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "room_type", nullable = false, length = 100)
    private String roomType;

    @Column(name = "multiplier", nullable = false, precision = 10, scale = 4)
    private BigDecimal multiplier;

    @Column(name = "season", nullable = false, length = 100)
    private String season;

    public PricingRule() {}

    public Long getId() { 
        return id; }
    public void setId(Long id) { 
        this.id = id; }

    public String getRoomType() { 
        return roomType; }
    public void setRoomType(String roomType) { 
        this.roomType = roomType; }

    public BigDecimal getMultiplier() { 
        return multiplier; }
    public void setMultiplier(BigDecimal multiplier) { 
        this.multiplier = multiplier; }

    public String getSeason() { 
        return season; }
    public void setSeason(String season) { 
        this.season = season; }
}