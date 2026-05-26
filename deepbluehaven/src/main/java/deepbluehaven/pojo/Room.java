package deepbluehaven.pojo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import deepbluehaven.pojo.enums.RoomStatus;
import deepbluehaven.pojo.enums.RoomTag;
import deepbluehaven.pojo.enums.RoomType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "Room")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resort_id", nullable = false)
    private Resort resort;

    @Column(name = "room_number", nullable = false, length = 50)
    private String roomNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false, length = 20)
    private RoomType roomType;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "room_tags", joinColumns = @JoinColumn(name = "room_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "tag", length = 30)
    private List<RoomTag> tags = new ArrayList<>();

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "room_images", joinColumns = @JoinColumn(name = "room_id"))
    @Column(name = "image_url", length = 255)
    private List<String> images = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private RoomStatus status;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Column(name = "base_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal basePrice;

    @OneToMany(mappedBy = "room", fetch = FetchType.LAZY)
    private List<RoomStatusLog> statusLogs = new ArrayList<>();

    public Room() {}

    public Long getId() { 
        return id; }
    public void setId(Long id) { 
        this.id = id; }

    public Resort getResort() { 
        return resort; }
    public void setResort(Resort resort) { 
        this.resort = resort; }

    public String getRoomNumber() { 
        return roomNumber; }
    public void setRoomNumber(String roomNumber) { 
        this.roomNumber = roomNumber; }

    public RoomType getRoomType() { 
        return roomType; }
    public void setRoomType(RoomType roomType) { 
        this.roomType = roomType; }
    public List<RoomTag> getTags() { 
        return tags; }
    public void setTags(List<RoomTag> tags) { 
        this.tags = tags; }

    public String getDescription() { 
        return description; }
    public void setDescription(String description) { 
        this.description = description; }

    public List<String> getImages() { 
        return images; }
    public void setImages(List<String> images) { 
        this.images = images; }
        
    public RoomStatus getStatus() { 
        return status; }
    public void setStatus(RoomStatus status) { 
        this.status = status; }

    public Integer getCapacity() { 
        return capacity; }
    public void setCapacity(Integer capacity) { 
        this.capacity = capacity; }

    public BigDecimal getBasePrice() { 
        return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { 
        this.basePrice = basePrice; }

    public List<RoomStatusLog> getStatusLogs() { 
        return statusLogs; }
    public void setStatusLogs(List<RoomStatusLog> statusLogs) { 
        this.statusLogs = statusLogs; }
}