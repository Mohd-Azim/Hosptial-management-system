package com.hospital.hms.repo;

import com.hospital.hms.domain.CanteenMenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CanteenMenuItemRepository extends JpaRepository<CanteenMenuItem, Long> {
}
