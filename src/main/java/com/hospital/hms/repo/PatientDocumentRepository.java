package com.hospital.hms.repo;

import com.hospital.hms.domain.PatientDocument;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientDocumentRepository extends JpaRepository<PatientDocument, Long> {

    List<PatientDocument> findByPatient_IdOrderByCreatedAtDesc(Long patientUserId);
}
