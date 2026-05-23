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
@Table(name = "Resort")
public class Resort {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "location", nullable = false, length = 255)
    private String location;

    @Column(name = "script", columnDefinition = "TEXT")
    private String script;

    @OneToMany(mappedBy = "resort", fetch = FetchType.LAZY)
    private List<Room> rooms = new ArrayList<>();

    public Resort() {}

    public Long getId() { 
        return id; }
    public void setId(Long id) { 
        this.id = id; }

    public String getName() { 
        return name; }
    public void setName(String name) { 
        this.name = name; }

    public String getLocation() { 
        return location; }
    public void setLocation(String location) { 
        this.location = location; }

    public String getScript() { return script; }
    public void setScript(String script) { 
        this.script = script; }

    public List<Room> getRooms() { 
        return rooms; }
    public void setRooms(List<Room> rooms) { 
        this.rooms = rooms; }
}
