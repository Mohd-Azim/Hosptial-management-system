package com.hospital.hms.repo;

import com.hospital.hms.domain.StaffSalaryAssignment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StaffSalaryAssignmentRepository extends JpaRepository<StaffSalaryAssignment, Long> {

    List<StaffSalaryAssignment> findByStaff_IdOrderByEffectiveFromDesc(Long staffUserId);

    @Query("select a from StaffSalaryAssignment a where a.staff.id = :staffId "
            + "and a.effectiveFrom <= :periodEnd "
            + "and (a.effectiveTo is null or a.effectiveTo >= :periodStart)")
    List<StaffSalaryAssignment> findActiveBetween(@Param("staffId") Long staffUserId,
            @Param("periodStart") java.time.LocalDate periodStart,
            @Param("periodEnd") java.time.LocalDate periodEnd);

    @Query("select distinct a.staff.id from StaffSalaryAssignment a where "
            + "a.effectiveFrom <= :periodEnd and (a.effectiveTo is null or a.effectiveTo >= :periodStart)")
    List<Long> findDistinctStaffIdsWithAssignmentOverlapping(@Param("periodStart") java.time.LocalDate periodStart,
            @Param("periodEnd") java.time.LocalDate periodEnd);
}
