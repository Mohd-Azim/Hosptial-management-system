package com.hospital.hms.repo;

import com.hospital.hms.domain.LeaveRequest;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByStaff_IdOrderByCreatedAtDesc(Long staffUserId);
}
