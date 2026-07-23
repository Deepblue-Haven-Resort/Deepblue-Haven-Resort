package deepbluehaven.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import deepbluehaven.pojo.Service; 
import deepbluehaven.pojo.enums.ServiceStatus; 

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {
        List<Service> findByStatus(ServiceStatus status);
    
}