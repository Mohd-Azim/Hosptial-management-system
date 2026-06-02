package com.hospital.hms.repo;

import com.hospital.hms.domain.MedicalWasteLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicalWasteLogRepository extends JpaRepository<MedicalWasteLog, Long> {
}
