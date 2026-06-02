package com.hospital.hms.service;

import com.hospital.hms.auth.UserPrincipal;
import com.hospital.hms.domain.Vendor;
import com.hospital.hms.domain.enums.RoleCode;
import com.hospital.hms.domain.enums.VendorPartyType;
import com.hospital.hms.repo.VendorRepository;
import com.hospital.hms.web.dto.VendorUpsertRequest;
import com.hospital.hms.web.error.ApiException;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VendorService {

    private final VendorRepository vendorRepository;

    public VendorService(VendorRepository vendorRepository) {
        this.vendorRepository = vendorRepository;
    }

    private static void assertProcurement(UserPrincipal actor) {
        if (!actor.hasAny(RoleCode.PROCUREMENT, RoleCode.ADMIN)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Procurement or admin only");
        }
    }

    @Transactional(readOnly = true)
    public List<Vendor> list(UserPrincipal actor) {
        assertProcurement(actor);
        return vendorRepository.findAll();
    }

    @Transactional
    public Vendor create(UserPrincipal actor, VendorUpsertRequest req) {
        assertProcurement(actor);
        Vendor v = new Vendor();
        apply(v, req);
        v.setCreatedAt(Instant.now());
        v.setUpdatedAt(Instant.now());
        return vendorRepository.save(v);
    }

    @Transactional
    public Vendor update(UserPrincipal actor, Long id, VendorUpsertRequest req) {
        assertProcurement(actor);
        Vendor v = vendorRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Vendor not found"));
        apply(v, req);
        v.setUpdatedAt(Instant.now());
        return vendorRepository.save(v);
    }

    @Transactional
    public void softDelete(UserPrincipal actor, Long id) {
        assertProcurement(actor);
        Vendor v = vendorRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Vendor not found"));
        v.setDeletedAt(Instant.now());
        v.setUpdatedAt(Instant.now());
        vendorRepository.save(v);
    }

    private void apply(Vendor v, VendorUpsertRequest req) {
        try {
            v.setPartyType(VendorPartyType.valueOf(req.partyType()));
        } catch (IllegalArgumentException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid partyType: use SUPPLIER, SERVICE_VENDOR, or BOTH");
        }
        v.setLegalName(req.legalName());
        v.setTradeName(req.tradeName());
        v.setGstin(req.gstin());
        v.setPan(req.pan());
        v.setContactPerson(req.contactPerson());
        v.setPhone(req.phone());
        v.setEmail(req.email());
        v.setAddress(req.address());
        v.setPaymentTermsDays(req.paymentTermsDays() != null ? req.paymentTermsDays() : 0);
        v.setActive(req.active() == null || req.active());
    }
}
