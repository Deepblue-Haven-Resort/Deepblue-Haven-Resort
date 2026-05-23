package deepbluehaven.pojo;
import deepbluehaven.pojo.enums.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "Worker_Profile")
public class WorkerProfile {

    @Id
    @Column(name = "worker_id")
    private Long workerId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "worker_id", nullable = false)
    private Worker worker;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 30)
    private Role role;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "email", nullable = true, length = 120)
    private String email;

    public WorkerProfile() {}

    public Long getWorkerId() { 
        return workerId; }
    public void setWorkerId(Long workerId) { 
        this.workerId = workerId; }

    public Worker getWorker() { 
        return worker; }
    public void setWorker(Worker worker) { 
        this.worker = worker; }

    public String getFullName() { 
        return fullName; }
    public void setFullName(String fullName) { 
        this.fullName = fullName; }

    public Role getRole() { 
        return role; }
    public void setRole(Role role) { 
        this.role = role; }

    public String getPhoneNumber() { 
        return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { 
        this.phoneNumber = phoneNumber; }

    public String getEmail() { 
        return email; }
    public void setEmail(String email) { 
        this.email = email; }
}
