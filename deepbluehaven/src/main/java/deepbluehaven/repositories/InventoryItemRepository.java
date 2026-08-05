package deepbluehaven.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import deepbluehaven.pojo.InventoryItem;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    @Query("SELECT i FROM InventoryItem i WHERE i.quantity <= i.minThreshold")
    List<InventoryItem> findLowInventoryItems();
}
