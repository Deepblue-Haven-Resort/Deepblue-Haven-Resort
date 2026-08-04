package deepbluehaven.pojo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import deepbluehaven.pojo.enums.PermissionTag;
import deepbluehaven.pojo.enums.Role;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "Worker_Table")
public class Worker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "employee_code", nullable = false, unique = true, length = 50)
    private String employeeCode;

    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @OneToMany(mappedBy = "worker", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkerRoleTag> roleTags = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private WorkerStatus status;

    @Column(name = "is_locked", nullable = false)
    private Boolean locked = false;

    @Column(name = "force_change_password", nullable = false)
    private Boolean forceChangePassword = true;

    @OneToOne(mappedBy = "worker", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private WorkerProfile profile;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "worker", fetch = FetchType.LAZY)
    private List<WorkerRoomAssignmentLog> assignmentLogs = new ArrayList<>();

    @OneToMany(mappedBy = "worker", fetch = FetchType.LAZY)
    private List<Notification> notifications = new ArrayList<>();

    public Worker() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public List<WorkerRoleTag> getRoleTags() {
        return roleTags;
    }

    public void setRoleTags(List<WorkerRoleTag> roleTags) {
        this.roleTags = roleTags;
    }

    public WorkerStatus getStatus() {
        return status;
    }

    public void setStatus(WorkerStatus status) {
        this.status = status;
    }

    public WorkerProfile getProfile() {
        return profile;
    }

    public void setProfile(WorkerProfile profile) {
        this.profile = profile;
    }

    public List<WorkerRoomAssignmentLog> getAssignmentLogs() {
        return assignmentLogs;
    }

    public void setAssignmentLogs(List<WorkerRoomAssignmentLog> assignmentLogs) {
        this.assignmentLogs = assignmentLogs;
    }

    public List<Notification> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
    }

    public boolean hasPermission(PermissionTag permission) {
        if (permission == null) {
            return false;
        }

        return roleTags.stream()
                .anyMatch(roleTag -> roleTag.getPermissionTag() == permission);
    }

    public void addPermission(
            PermissionTag permission,
            String description) {

        if (permission == null || hasPermission(permission)) {
            return;
        }

        WorkerRoleTag roleTag = new WorkerRoleTag(
                this,
                permission,
                description);

        roleTags.add(roleTag);
    }

    public void removePermission(PermissionTag permission) {
        if (permission == null) {
            return;
        }

        roleTags.removeIf(roleTag -> roleTag.getPermissionTag() == permission);
    }

    public void syncPermissions(
            Set<PermissionTag> permissions,
            String description) {

        if (permissions == null) {
            throw new IllegalArgumentException(
                    "Danh sách permission không được null");
        }

        roleTags.removeIf(roleTag -> !permissions.contains(roleTag.getPermissionTag()));

        for (PermissionTag permission : permissions) {
            addPermission(permission, description);
        }
    }

    public void applyDefaultPermissions(Role role) {
        if (role == null) {
            throw new IllegalArgumentException(
                    "Role không được null");
        }

        syncPermissions(
                role.getDefaultPermissions(),
                "Quyền mặc định của role " + role.name());
    }

    public Set<PermissionTag> getPermissions() {
        Set<PermissionTag> permissions = EnumSet.noneOf(PermissionTag.class);

        for (WorkerRoleTag roleTag : roleTags) {
            permissions.add(roleTag.getPermissionTag());
        }

        return Set.copyOf(permissions);
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public Boolean getLocked() {
        return locked;
    }

    public void setLocked(Boolean locked) {
        this.locked = locked;
    }

    public Boolean getForceChangePassword() {
        return forceChangePassword;
    }

    public void setForceChangePassword(Boolean forceChangePassword) {
        this.forceChangePassword = forceChangePassword;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @PrePersist
    private void initializeCreatedAt() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
