package com.hospital.hms.repo;

import com.hospital.hms.domain.PayrollComponent;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayrollComponentRepository extends JpaRepository<PayrollComponent, Long> {

    Optional<PayrollComponent> findByCode(String code);
}
