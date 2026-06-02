package com.hospital.hms.repo;

import com.hospital.hms.domain.AttendanceLog;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceLogRepository extends JpaRepository<AttendanceLog, Long> {

    Optional<AttendanceLog> findByStaff_IdAndLogDate(Long staffUserId, LocalDate logDate);

    List<AttendanceLog> findByStaff_IdOrderByLogDateDesc(Long staffUserId);

    List<AttendanceLog> findByLogDateOrderByStaff_IdAsc(LocalDate logDate);

    List<AttendanceLog> findByStaff_IdAndLogDateBetweenOrderByLogDateAsc(Long staffUserId, LocalDate from,
            LocalDate to);
}
