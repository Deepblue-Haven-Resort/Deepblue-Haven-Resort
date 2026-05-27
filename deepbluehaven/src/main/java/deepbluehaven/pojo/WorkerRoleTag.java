package deepbluehaven.pojo;

import deepbluehaven.pojo.enums.RoleTag;
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
    @Column(name = "role_tag", nullable = false, length = 50)
    private RoleTag roleTag;

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
    public RoleTag getRoleTag() { 
        return roleTag; }
    public void setRoleTag(RoleTag roleTag) {
        this.roleTag = roleTag; }
    public String getDescription() { 
        return description; }
    public void setDescription(String description) { 
        this.description = description; }

}