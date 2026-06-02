package com.hospital.hms.repo;

import com.hospital.hms.domain.Admission;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdmissionRepository extends JpaRepository<Admission, Long> {

    List<Admission> findByPatient_IdAndDischargedAtIsNull(Long patientUserId);
}
