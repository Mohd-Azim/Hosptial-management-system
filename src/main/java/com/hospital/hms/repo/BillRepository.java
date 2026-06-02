package com.hospital.hms.repo;

import com.hospital.hms.domain.Bill;
import com.hospital.hms.domain.enums.BillStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillRepository extends JpaRepository<Bill, Long> {

    List<Bill> findByPatient_IdOrderByCreatedAtDesc(Long patientUserId);

    Optional<Bill> findByReferenceTypeAndReferenceId(String referenceType, Long referenceId);

    List<Bill> findByReferenceTypeAndStatusOrderByCreatedAtDesc(String referenceType, BillStatus status);
}
