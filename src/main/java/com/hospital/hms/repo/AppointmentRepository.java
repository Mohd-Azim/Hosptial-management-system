package com.hospital.hms.repo;

import com.hospital.hms.domain.Appointment;
import com.hospital.hms.domain.enums.AppointmentStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPatient_Id(Long patientUserId);

    List<Appointment> findByStatusAndScheduledAtBetweenOrderByScheduledAtAsc(
            AppointmentStatus status,
            Instant start,
            Instant end);

    /** Indexed by calendar day (IST) — preferred for high-volume daily queues. */
    List<Appointment> findByStatusAndScheduledLocalDateOrderByScheduledAtAsc(
            AppointmentStatus status,
            LocalDate day);
}
