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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "Customer")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private CustomerProfile profile;

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    private List<CustomerDiscount> discounts = new ArrayList<>();

    public Customer() {}

    public Long getId() { 
        return id; }
    public void setId(Long id) { 
        this.id = id; }

    public String getUsername() { 
        return username; }
    public void setUsername(String username) { 
        this.username = username; }

    public String getPasswordHash() { 
        return passwordHash; }
    public void setPasswordHash(String passwordHash) { 
        this.passwordHash = passwordHash; }

    public CustomerProfile getProfile() { 
        return profile; }
    public void setProfile(CustomerProfile profile) { 
        this.profile = profile; }

    public List<CustomerDiscount> getDiscounts() { 
        return discounts; }
    public void setDiscounts(List<CustomerDiscount> discounts) { 
        this.discounts = discounts; }
}