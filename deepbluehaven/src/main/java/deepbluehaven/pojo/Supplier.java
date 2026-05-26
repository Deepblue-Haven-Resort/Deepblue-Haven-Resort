package deepbluehaven.pojo;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "Supplier")
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "email", nullable = false, length = 120)
    private String email;

    @Column(name = "address", nullable = false, length = 255)
    private String address;

    @OneToMany(mappedBy = "supplier", fetch = FetchType.LAZY)
    private List<InventoryItem> items = new ArrayList<>();

    public Supplier() {}

    public Long getId() { 
        return id; }
    public void setId(Long id) { 
        this.id = id; }

    public String getName() { 
        return name; }
    public void setName(String name) { 
        this.name = name; }

    public String getPhoneNumber() { 
        return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { 
        this.phoneNumber = phoneNumber; }

    public String getEmail() { 
        return email; }
    public void setEmail(String email) { 
        this.email = email; }

    public String getAddress() { 
        return address; }
    public void setAddress(String address) { 
        this.address = address; }

    public List<InventoryItem> getItems() { 
        return items; }
    public void setItems(List<InventoryItem> items) { 
        this.items = items; }
}
