package deepbluehaven.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import deepbluehaven.pojo.Resort;

public interface ResortRepository extends JpaRepository<Resort, Long> {
}
