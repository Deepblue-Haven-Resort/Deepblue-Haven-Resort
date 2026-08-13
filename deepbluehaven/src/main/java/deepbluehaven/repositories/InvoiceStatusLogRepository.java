package deepbluehaven.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import deepbluehaven.pojo.InvoiceStatusLog;

@Repository
public interface InvoiceStatusLogRepository extends JpaRepository<InvoiceStatusLog, Long> {

    List<InvoiceStatusLog> findTop10ByOrderByTimestampDesc();
    List<InvoiceStatusLog> findAllByOrderByTimestampDesc();
}
