package com.hospital.hms.repo;

import com.hospital.hms.domain.Prescription;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    Optional<Prescription> findByVisit_Id(Long visitId);

    @Query("select p from Prescription p join p.visit v join v.appointment a "
            + "where a.patient.id = :patientUserId order by p.createdAt desc")
    List<Prescription> findAllForPatient(@Param("patientUserId") Long patientUserId);
}
