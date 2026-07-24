package deepbluehaven.pojo;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "Inventory_Item")
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resort_id", nullable = false)
    private Resort resort;

    @Column(name = "name", nullable = false, length = 150, columnDefinition = "NVARCHAR(150)")
    private String name;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit", nullable = false, length = 50, columnDefinition = "NVARCHAR(50)")
    private String unit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @OneToMany(mappedBy = "inventoryItem", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<InventoryTransaction> transactions = new ArrayList<>();

    public InventoryItem() {}

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

    public Integer getQuantity() { 
        return quantity; }
    public void setQuantity(Integer quantity) { 
        this.quantity = quantity; }

    public String getUnit() { 
        return unit; }
    public void setUnit(String unit) { 
        this.unit = unit; }

    public Supplier getSupplier() { 
        return supplier; }
    public void setSupplier(Supplier supplier) { 
        this.supplier = supplier; }

    public List<InventoryTransaction> getTransactions() { 
        return transactions; }
    public void setTransactions(List<InventoryTransaction> transactions) { 
        this.transactions = transactions; }
}