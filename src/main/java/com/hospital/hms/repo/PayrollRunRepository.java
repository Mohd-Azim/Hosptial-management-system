package com.hospital.hms.repo;

import com.hospital.hms.domain.PayrollRun;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayrollRunRepository extends JpaRepository<PayrollRun, Long> {

    Optional<PayrollRun> findByPeriodYearAndPeriodMonth(int year, int month);
}
