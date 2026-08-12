package deepbluehaven.pojo;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.CheckConstraint;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(
    name = "Customer_Profile",
    check = @CheckConstraint(
        name = "Check_email_or_phone",
        constraint = "email IS NOT NULL OR phone_number IS NOT NULL"
    )
)
public class CustomerProfile {

    @Id
    @Column(name = "customer_id")
    private Long customerId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "birth_day", nullable = true)
    private LocalDate birthDay;

    @Column(name = "full_name", nullable = false, length = 50, columnDefinition = "NVARCHAR(50)")
    private String fullName;

    @Column(name = "phone_number", nullable = true, length = 20)
    private String phoneNumber;

    @Column(name = "email", nullable = true, length = 120)
    private String email;

    @Column(name = "total_bookings", nullable = false)
    private Integer totalBookings = 0;

    @Column(name = "total_spent", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalSpent = BigDecimal.ZERO;

    @Column(name = "total_points", nullable = false)
    private Integer totalPoints = 0;

    @Column(name = "segment", nullable = true, length = 50)
    private String segment;

    @Column(name = "avatar_url", nullable = true, length = 550)
    private String avatarUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "membership_tier_id", nullable = true)
    private MembershipTier membershipTier;

    public CustomerProfile() {}

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public Long getCustomerId() { 
        return customerId; }
    public void setCustomerId(Long customerId) { 
        this.customerId = customerId; }

    public Customer getCustomer() { 
        return customer; }
    public void setCustomer(Customer customer) { 
        this.customer = customer; }

    public LocalDate getBirthDay() { 
        return birthDay; }
    public void setBirthDay(LocalDate birthDay) { 
        this.birthDay = birthDay; }

    public String getFullName() { 
        return fullName; }
    public void setFullName(String fullName) { 
        this.fullName = fullName; }

    public String getPhoneNumber() { 
        return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { 
        this.phoneNumber = phoneNumber; }

    public String getEmail() { 
        return email; }
    public void setEmail(String email) { 
        this.email = email; }

    public Integer getTotalBookings() { 
        return totalBookings; }
    public void setTotalBookings(Integer totalBookings) { 
        this.totalBookings = totalBookings; }

    public BigDecimal getTotalSpent() { 
        return totalSpent; }
    public void setTotalSpent(BigDecimal totalSpent) { 
        this.totalSpent = totalSpent; }

    public Integer getTotalPoints() { 
        return totalPoints; }
    public void setTotalPoints(Integer totalPoints) { 
        this.totalPoints = totalPoints; }

    public String getSegment() { 
        return segment; }
    public void setSegment(String segment) { 
        this.segment = segment; }

    public MembershipTier getMembershipTier() { 
        return membershipTier; }
    public void setMembershipTier(MembershipTier membershipTier) { 
        this.membershipTier = membershipTier; }
}
