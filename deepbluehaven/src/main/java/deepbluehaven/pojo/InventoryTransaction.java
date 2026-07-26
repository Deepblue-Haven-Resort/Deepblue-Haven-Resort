package deepbluehaven.pojo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "Inventory_Transaction")
public class InventoryTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private InventoryItem inventoryItem;

    @Column(name = "change_amount", nullable = false)
    private Integer changeAmount;

    @Column(name = "reason", nullable = false, length = 255, columnDefinition = "NVARCHAR(255)")
    private String reason;

    public InventoryTransaction() {}

    public Long getId() { 
        return id; }
    public void setId(Long id) { 
        this.id = id; }

    public InventoryItem getInventoryItem() { 
        return inventoryItem; }
    public void setInventoryItem(InventoryItem inventoryItem) { 
        this.inventoryItem = inventoryItem; }

    public Integer getChangeAmount() { 
        return changeAmount; }
    public void setChangeAmount(Integer changeAmount) { 
        this.changeAmount = changeAmount; }

    public String getReason() { 
        return reason; }
    public void setReason(String reason) { 
        this.reason = reason; }
}