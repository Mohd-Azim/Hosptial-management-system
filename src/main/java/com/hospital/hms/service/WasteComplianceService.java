package com.hospital.hms.service;

import com.hospital.hms.auth.UserPrincipal;
import com.hospital.hms.domain.MedicalWasteLog;
import com.hospital.hms.domain.User;
import com.hospital.hms.domain.enums.RoleCode;
import com.hospital.hms.repo.MedicalWasteLogRepository;
import com.hospital.hms.repo.UserRepository;
import com.hospital.hms.web.dto.MedicalWasteLogRequest;
import com.hospital.hms.web.error.ApiException;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WasteComplianceService {

    private final MedicalWasteLogRepository wasteLogRepository;
    private final UserRepository userRepository;

    public WasteComplianceService(MedicalWasteLogRepository wasteLogRepository, UserRepository userRepository) {
        this.wasteLogRepository = wasteLogRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public MedicalWasteLog log(UserPrincipal actor, MedicalWasteLogRequest req) {
        if (!actor.hasAny(RoleCode.ADMIN, RoleCode.NURSE, RoleCode.PHARMACY, RoleCode.DOCTOR)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Not authorised to log BMW waste");
        }
        User recorder = userRepository.findById(actor.userId()).orElseThrow();
        MedicalWasteLog log = new MedicalWasteLog();
        log.setCategoryCode(req.categoryCode());
        log.setDescription(req.description());
        log.setQuantityValue(req.quantityValue());
        log.setQuantityUnit(req.quantityUnit());
        log.setSegregationStatus(req.segregationStatus());
        log.setStorageLocation(req.storageLocation());
        log.setHandedOverToAuthorisedAgent(req.handedOverToAuthorisedAgent());
        log.setCbwtfManifestNumber(req.cbwtfManifestNumber());
        log.setRecordedBy(recorder);
        log.setIncidentNotes(req.incidentNotes());
        log.setRecordedAt(Instant.now());
        log.setCreatedAt(Instant.now());
        return wasteLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public List<MedicalWasteLog> list(UserPrincipal actor) {
        if (!actor.hasAny(RoleCode.ADMIN, RoleCode.NURSE)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Not authorised");
        }
        return wasteLogRepository.findAll();
    }
}
