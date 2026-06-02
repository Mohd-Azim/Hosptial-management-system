package com.hospital.hms.service;

import com.hospital.hms.auth.UserPrincipal;
import com.hospital.hms.domain.Admission;
import com.hospital.hms.domain.Bed;
import com.hospital.hms.domain.User;
import com.hospital.hms.domain.enums.BedStatus;
import com.hospital.hms.domain.enums.PaymentMode;
import com.hospital.hms.domain.enums.RoleCode;
import com.hospital.hms.repo.AdmissionRepository;
import com.hospital.hms.repo.BedRepository;
import com.hospital.hms.repo.UserRepository;
import com.hospital.hms.repo.WardRepository;
import com.hospital.hms.support.BillRefs;
import com.hospital.hms.domain.Ward;
import com.hospital.hms.web.error.ApiException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FacilityService {

    private final WardRepository wardRepository;
    private final BedRepository bedRepository;
    private final AdmissionRepository admissionRepository;
    private final UserRepository userRepository;
    private final BillingService billingService;

    @Value("${hms.admission-fee:5000}")
    private BigDecimal admissionFee;

    public FacilityService(WardRepository wardRepository,
            BedRepository bedRepository,
            AdmissionRepository admissionRepository,
            UserRepository userRepository,
            BillingService billingService) {
        this.wardRepository = wardRepository;
        this.bedRepository = bedRepository;
        this.admissionRepository = admissionRepository;
        this.userRepository = userRepository;
        this.billingService = billingService;
    }

    @Transactional(readOnly = true)
    public List<Ward> listWards() {
        return wardRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Bed> availableBeds(Long wardId) {
        return bedRepository.findByWard_IdAndStatus(wardId, BedStatus.AVAILABLE);
    }

    /**
     * Pay-first MVP: if admission fee &gt; 0, reception must collect offline payment in the same request.
     */
    @Transactional
    public Admission admit(UserPrincipal actor, Long patientUserId, Long bedId, String admissionType,
            boolean offlinePaidImmediate) {
        if (!actor.hasAny(RoleCode.RECEPTION, RoleCode.ADMIN, RoleCode.NURSE)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Staff only");
        }
        User patient = userRepository.findById(patientUserId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Patient not found"));
        Bed bed = bedRepository.findById(bedId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Bed not found"));
        if (bed.getStatus() != BedStatus.AVAILABLE) {
            throw new ApiException(HttpStatus.CONFLICT, "Bed not available");
        }
        BigDecimal fee = admissionFee != null ? admissionFee : BigDecimal.ZERO;
        if (fee.compareTo(BigDecimal.ZERO) > 0 && !offlinePaidImmediate) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Admission fee requires immediate offline payment at desk (pay-first MVP)");
        }
        Instant now = Instant.now();
        Admission adm = new Admission();
        adm.setPatient(patient);
        adm.setBed(bed);
        adm.setAdmissionType(admissionType);
        adm.setAdmittedAt(now);
        adm.setCreatedAt(now);
        adm.setUpdatedAt(now);
        admissionRepository.save(adm);

        if (fee.compareTo(BigDecimal.ZERO) > 0) {
            var bill = billingService.createBill(patient, BillRefs.ADMISSION, adm.getId(), fee, BigDecimal.ZERO, false);
            UserPrincipal cashier = new UserPrincipal(actor.userId(), "", java.util.Set.of(RoleCode.RECEPTION));
            billingService.payBill(cashier, bill.getId(), PaymentMode.OFFLINE, bill.getTotalAmount(),
                    "admission-desk", actor.userId());
        }

        bed.setStatus(BedStatus.OCCUPIED);
        bed.setUpdatedAt(now);
        bedRepository.save(bed);

        return admissionRepository.findById(adm.getId()).orElseThrow();
    }

    @Transactional
    public Admission discharge(UserPrincipal actor, Long admissionId) {
        if (!actor.hasAny(RoleCode.RECEPTION, RoleCode.ADMIN, RoleCode.NURSE, RoleCode.DOCTOR)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Staff only");
        }
        Admission adm = admissionRepository.findById(admissionId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Admission not found"));
        if (adm.getDischargedAt() != null) {
            throw new ApiException(HttpStatus.CONFLICT, "Already discharged");
        }
        Instant now = Instant.now();
        adm.setDischargedAt(now);
        adm.setUpdatedAt(now);
        Bed bed = adm.getBed();
        bed.setStatus(BedStatus.AVAILABLE);
        bed.setUpdatedAt(now);
        bedRepository.save(bed);
        return admissionRepository.save(adm);
    }
}
