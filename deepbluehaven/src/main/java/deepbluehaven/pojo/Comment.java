package deepbluehaven.pojo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "Comment")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resort_id")
    private Resort resort;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private Service service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id")
    private Worker worker;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "comment_images", joinColumns = @JoinColumn(name = "comment_id"))
    @Column(name = "image_url", length = 255)
    private List<String> images = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Comment() {}


    public Long getId() { 
        return id; }
    public void setId(Long id) { 
        this.id = id; }

    public Customer getCustomer() { 
        return customer; }
    public void setCustomer(Customer customer) { 
        this.customer = customer; }

    public Resort getResort() { 
        return resort; }
    public void setResort(Resort resort) { 
        this.resort = resort; }

    public Room getRoom() { 
        return room; }
    public void setRoom(Room room) { 
        this.room = room; }

    public Service getService() { 
        return service; }
    public void setService(Service service) { 
        this.service = service; }

    public Worker getWorker() { 
        return worker; }
    public void setWorker(Worker worker) { 
        this.worker = worker; }

    public String getContent() { 
        return content; }
    public void setContent(String content) { 
        this.content = content; }

    public Integer getRating() { 
        return rating; }
    public void setRating(Integer rating) { 
        this.rating = rating; }

    public List<String> getImages() { 
        return images; }
    public void setImages(List<String> images) { 
        this.images = images; }

    public LocalDateTime getCreatedAt() { 
        return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { 
        this.createdAt = createdAt; }
}