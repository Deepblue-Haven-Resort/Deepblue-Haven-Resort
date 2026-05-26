package deepbluehaven.pojo;

import java.math.BigDecimal;
import java.time.LocalDate;

import deepbluehaven.pojo.enums.RoomType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Pricing_Rule")
public class PricingRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false, length = 20)
    private RoomType roomType;

    @Column(name = "multiplier", nullable = false, precision = 10, scale = 4)
    private BigDecimal multiplier;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    public PricingRule() {}

    public Long getId() { 
        return id; }
    public void setId(Long id) { 
        this.id = id; }

    public RoomType getRoomType() { 
        return roomType; }
    public void setRoomType(RoomType roomType) { 
        this.roomType = roomType; }

    public BigDecimal getMultiplier() { 
        return multiplier; }
    public void setMultiplier(BigDecimal multiplier) { 
        this.multiplier = multiplier; }

    public LocalDate getStartDate() { 
        return startDate; }
    public void setStartDate(LocalDate startDate) { 
        this.startDate = startDate; }

    public LocalDate getEndDate() { 
        return endDate; }
    public void setEndDate(LocalDate endDate) { 
        this.endDate = endDate; }
}