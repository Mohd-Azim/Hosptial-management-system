package com.hospital.hms.repo;

import com.hospital.hms.domain.CanteenOrder;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CanteenOrderRepository extends JpaRepository<CanteenOrder, Long> {

    List<CanteenOrder> findByPlacedBy_IdOrderByCreatedAtDesc(Long userId);
}
