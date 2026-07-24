package deepbluehaven.pojo;

import deepbluehaven.pojo.enums.PermissionTag;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "Worker_Role_Tag",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_worker_permission",
                        columnNames = {"worker_id", "permission_tag"}
                )
        }
)
public class WorkerRoleTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "worker_id", nullable = false)
    private Worker worker;

    @Enumerated(EnumType.STRING)
    @Column(name = "permission_tag", nullable = false, length = 50)
    private PermissionTag permissionTag;

    @Column(name = "description", length = 500, columnDefinition = "NVARCHAR(500)")
    private String description;

    public WorkerRoleTag() {
    }

    public WorkerRoleTag(
            Worker worker,
            PermissionTag permissionTag,
            String description) {

        this.worker = worker;
        this.permissionTag = permissionTag;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public Worker getWorker() {
        return worker;
    }

    public void setWorker(Worker worker) {
        this.worker = worker;
    }

    public PermissionTag getPermissionTag() {
        return permissionTag;
    }

    public void setPermissionTag(PermissionTag permissionTag) {
        this.permissionTag = permissionTag;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}