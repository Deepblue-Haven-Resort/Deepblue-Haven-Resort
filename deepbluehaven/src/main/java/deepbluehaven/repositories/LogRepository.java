package deepbluehaven.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import deepbluehaven.pojo.Log;

import deepbluehaven.pojo.enums.ObjectType;

@Repository
public interface LogRepository extends JpaRepository<Log, Long> {

    List<Log> findTop10ByOrderByTimestampDesc();
    List<Log> findAllByOrderByTimestampDesc();
    List<Log> findByObjectTypeInOrderByTimestampDesc(List<ObjectType> objectTypes);
    List<Log> findTop10ByObjectTypeInOrderByTimestampDesc(List<ObjectType> objectTypes);
}
