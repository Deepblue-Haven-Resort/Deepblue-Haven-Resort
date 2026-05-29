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

@Entity
@Table(name = "Worker_Role_Tag")
public class WorkerRoleTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", nullable = false)
    private Worker worker;

    @Enumerated(EnumType.STRING)
    @Column(name = "permission_tag", nullable = false, length = 50)
    private PermissionTag permissionTag;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    public WorkerRoleTag() {}

    public Long getId() { 
        return id; }
    public void setId(Long id) { 
        this.id = id; }
    public Worker getWorker() { 
        return worker; }
    public void setWorker(Worker worker) { 
        this.worker = worker; }
    public PermissionTag getPermissionTag() { 
        return permissionTag; }
    public void setPermissionTag(PermissionTag permissionTag) {
        this.permissionTag = permissionTag; }
    public String getDescription() { 
        return description; }
    public void setDescription(String description) { 
        this.description = description; }

}