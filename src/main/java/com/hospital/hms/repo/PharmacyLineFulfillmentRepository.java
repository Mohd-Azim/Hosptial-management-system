package com.hospital.hms.repo;

import com.hospital.hms.domain.PharmacyLineFulfillment;
import com.hospital.hms.domain.enums.PharmacyFulfillmentStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PharmacyLineFulfillmentRepository extends JpaRepository<PharmacyLineFulfillment, Long> {

    List<PharmacyLineFulfillment> findByStatus(PharmacyFulfillmentStatus status);

    @Query("select distinct f from PharmacyLineFulfillment f "
            + "join fetch f.prescriptionLine pl "
            + "join fetch pl.prescription p "
            + "join fetch p.visit v "
            + "join fetch v.appointment a "
            + "join fetch a.patient "
            + "where f.status = :status")
    List<PharmacyLineFulfillment> findPendingDetailed(@Param("status") PharmacyFulfillmentStatus status);

    Optional<PharmacyLineFulfillment> findByPrescriptionLine_Id(Long lineId);

    List<PharmacyLineFulfillment> findByBill_Id(Long billId);
}
