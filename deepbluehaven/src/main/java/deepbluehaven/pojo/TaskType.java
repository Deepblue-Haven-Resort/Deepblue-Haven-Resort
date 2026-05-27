package deepbluehaven.pojo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Task_Type")
public class TaskType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "required_level", nullable = false)
    private Integer requiredLevel = 1;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    public TaskType() {}

    public Long getId() { 
        return id; }
    public void setId(Long id) { 
        this.id = id; }
    public String getName() { 
        return name; }
    public void setName(String name) { 
        this.name = name; }
    public String getDescription() { 
        return description; }
    public void setDescription(String description) { 
        this.description = description; }
    public Integer getRequiredLevel() { 
        return requiredLevel; }
    public void setRequiredLevel(Integer requiredLevel) { 
        this.requiredLevel = requiredLevel; }
    public Boolean getIsActive() { 
        return isActive; }
    public void setIsActive(Boolean isActive) { 
        this.isActive = isActive; }

}