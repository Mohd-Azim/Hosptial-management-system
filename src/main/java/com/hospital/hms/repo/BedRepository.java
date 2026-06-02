package com.hospital.hms.repo;

import com.hospital.hms.domain.Bed;
import com.hospital.hms.domain.enums.BedStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BedRepository extends JpaRepository<Bed, Long> {

    List<Bed> findByWard_IdAndStatus(Long wardId, BedStatus status);
}
