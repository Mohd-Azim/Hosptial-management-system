package com.hospital.hms.repo;

import com.hospital.hms.domain.PrescriptionLine;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrescriptionLineRepository extends JpaRepository<PrescriptionLine, Long> {

    List<PrescriptionLine> findByPrescription_Id(Long prescriptionId);
}
