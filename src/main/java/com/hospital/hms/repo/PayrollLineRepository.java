package com.hospital.hms.repo;

import com.hospital.hms.domain.PayrollLine;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayrollLineRepository extends JpaRepository<PayrollLine, Long> {

    List<PayrollLine> findByPayrollRun_IdOrderByStaff_IdAsc(Long payrollRunId);

    long countByPayrollRun_Id(Long payrollRunId);
}
