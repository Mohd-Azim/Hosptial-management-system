package com.hospital.hms.repo;

import com.hospital.hms.domain.Visit;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VisitRepository extends JpaRepository<Visit, Long> {

    Optional<Visit> findByAppointment_Id(Long appointmentId);
}
