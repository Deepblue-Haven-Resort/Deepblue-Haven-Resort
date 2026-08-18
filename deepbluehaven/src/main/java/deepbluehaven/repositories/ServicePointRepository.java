package deepbluehaven.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import deepbluehaven.pojo.ServicePoint;
import deepbluehaven.pojo.enums.ServiceCategory;

@Repository
public interface ServicePointRepository extends JpaRepository<ServicePoint, Long> {

    Optional<ServicePoint> findByServiceCategory(ServiceCategory serviceCategory);

    List<ServicePoint> findByIsActiveTrue();
}
