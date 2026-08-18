package deepbluehaven.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import deepbluehaven.pojo.InventoryItem;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    @Query("SELECT i FROM InventoryItem i LEFT JOIN FETCH i.supplier LEFT JOIN FETCH i.resort")
    List<InventoryItem> findAllWithSupplierAndResort();

    @Query("SELECT i FROM InventoryItem i WHERE i.quantity <= i.minThreshold")
    List<InventoryItem> findLowInventoryItems();

    @Query("SELECT i FROM InventoryItem i WHERE LOWER(i.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<InventoryItem> findByNameLike(@org.springframework.data.repository.query.Param("name") String name);
}
