package deepbluehaven.pojo;

import java.util.ArrayList;
import java.util.List;

import deepbluehaven.pojo.enums.WorkerStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "Worker_Table")
public class Worker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private WorkerStatus status;

    @OneToOne(mappedBy = "worker", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private WorkerProfile profile;

    @OneToMany(mappedBy = "worker", fetch = FetchType.LAZY)
    private List<WorkerRoomAssignmentLog> assignmentLogs = new ArrayList<>();

    public Worker() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public WorkerStatus getStatus() { return status; }
    public void setStatus(WorkerStatus status) { this.status = status; }

    public WorkerProfile getProfile() { return profile; }
    public void setProfile(WorkerProfile profile) { this.profile = profile; }

    public List<WorkerRoomAssignmentLog> getAssignmentLogs() { return assignmentLogs; }
    public void setAssignmentLogs(List<WorkerRoomAssignmentLog> assignmentLogs) { this.assignmentLogs = assignmentLogs; }
}
