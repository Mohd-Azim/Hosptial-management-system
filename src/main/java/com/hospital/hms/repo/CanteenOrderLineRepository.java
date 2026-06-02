package com.hospital.hms.repo;

import com.hospital.hms.domain.CanteenOrderLine;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CanteenOrderLineRepository extends JpaRepository<CanteenOrderLine, Long> {

    List<CanteenOrderLine> findByOrder_Id(Long orderId);
}
