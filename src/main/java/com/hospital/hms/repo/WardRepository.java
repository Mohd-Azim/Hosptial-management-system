package com.hospital.hms.repo;

import com.hospital.hms.domain.Ward;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WardRepository extends JpaRepository<Ward, Long> {
}
