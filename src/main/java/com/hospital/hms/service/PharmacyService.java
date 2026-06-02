package com.hospital.hms.service;

import com.hospital.hms.auth.UserPrincipal;
import com.hospital.hms.domain.Bill;
import com.hospital.hms.domain.PharmacyLineFulfillment;
import com.hospital.hms.domain.Prescription;
import com.hospital.hms.domain.User;
import com.hospital.hms.domain.enums.BillStatus;
import com.hospital.hms.domain.enums.PharmacyFulfillmentStatus;
import com.hospital.hms.domain.enums.RoleCode;
import com.hospital.hms.notification.NotificationEnvelope;
import com.hospital.hms.notification.NotificationProducer;
import com.hospital.hms.notification.NotificationType;
import com.hospital.hms.pharmacylock.PharmacyOperationLock;
import com.hospital.hms.repo.BillRepository;
import com.hospital.hms.repo.PharmacyLineFulfillmentRepository;
import com.hospital.hms.repo.UserRepository;
import com.hospital.hms.support.BillRefs;
import com.hospital.hms.web.error.ApiException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PharmacyService {

    private final PharmacyLineFulfillmentRepository fulfillmentRepository;
    private final BillingService billingService;
    private final BillRepository billRepository;
    private final UserRepository userRepository;
    private final PharmacyOperationLock pharmacyOperationLock;
    private final NotificationProducer notificationProducer;

    public PharmacyService(PharmacyLineFulfillmentRepository fulfillmentRepository,
            BillingService billingService,
            BillRepository billRepository,
            UserRepository userRepository,
            PharmacyOperationLock pharmacyOperationLock,
            NotificationProducer notificationProducer) {
        this.fulfillmentRepository = fulfillmentRepository;
        this.billingService = billingService;
        this.billRepository = billRepository;
        this.userRepository = userRepository;
        this.pharmacyOperationLock = pharmacyOperationLock;
        this.notificationProducer = notificationProducer;
    }

    @Transactional(readOnly = true)
    public List<PharmacyLineFulfillment> pending() {
        return fulfillmentRepository.findPendingDetailed(PharmacyFulfillmentStatus.PENDING);
    }

    @Transactional
    public Bill createBillForFulfillments(UserPrincipal actor, List<Long> fulfillmentIds) {
        if (!actor.hasAny(RoleCode.PHARMACY, RoleCode.ADMIN)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Pharmacy only");
        }
        List<PharmacyLineFulfillment> rows = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        Prescription rxRef = null;
        for (Long id : fulfillmentIds) {
            PharmacyLineFulfillment f = fulfillmentRepository.findById(id)
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Fulfillment not found: " + id));
            if (f.getStatus() != PharmacyFulfillmentStatus.PENDING) {
                throw new ApiException(HttpStatus.CONFLICT, "Fulfillment not pending");
            }
            if (f.getBill() != null) {
                throw new ApiException(HttpStatus.CONFLICT, "Bill already linked");
            }
            Prescription rx = f.getPrescriptionLine().getPrescription();
            if (rxRef == null) {
                rxRef = rx;
            } else if (!rxRef.getId().equals(rx.getId())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "All lines must belong to same prescription");
            }
            total = total.add(f.getPrescriptionLine().getUnitPrice());
            rows.add(f);
        }
        if (rxRef == null || rows.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "No fulfillments");
        }
        Long rxId = rxRef.getId();
        final Prescription finalRxRef = rxRef;
        final BigDecimal finalTotal = total;
        final List<PharmacyLineFulfillment> finalRows = rows;
        return pharmacyOperationLock.withPrescriptionLock(rxId, () -> createBillForFulfillmentsLocked(actor, finalRows, finalRxRef, finalTotal));
    }

    private Bill createBillForFulfillmentsLocked(UserPrincipal actor, List<PharmacyLineFulfillment> rows,
            Prescription rxRef, BigDecimal total) {
        User patient = rxRef.getVisit().getAppointment().getPatient();
        Bill bill = billingService.createBill(patient, BillRefs.PHARMACY, rxRef.getId(), total, BigDecimal.ZERO, false);
        for (PharmacyLineFulfillment f : rows) {
            f.setBill(bill);
            f.setUpdatedAt(Instant.now());
            fulfillmentRepository.save(f);
        }
        return billRepository.findById(bill.getId()).orElseThrow();
    }

    @Transactional
    public void declineFulfillment(UserPrincipal actor, Long fulfillmentId) {
        if (!actor.hasAny(RoleCode.PHARMACY, RoleCode.ADMIN)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Pharmacy only");
        }
        PharmacyLineFulfillment f = fulfillmentRepository.findById(fulfillmentId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Fulfillment not found"));
        if (f.getStatus() != PharmacyFulfillmentStatus.PENDING) {
            throw new ApiException(HttpStatus.CONFLICT, "Not pending");
        }
        if (f.getBill() != null) {
            throw new ApiException(HttpStatus.CONFLICT, "Bill already created");
        }
        f.setStatus(PharmacyFulfillmentStatus.DECLINED);
        f.setUpdatedAt(Instant.now());
        fulfillmentRepository.save(f);
    }

    @Transactional
    public void dispenseBill(UserPrincipal actor, Long billId) {
        if (!actor.hasAny(RoleCode.PHARMACY, RoleCode.ADMIN)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Pharmacy only");
        }
        pharmacyOperationLock.withBillLock(billId, () -> {
            dispenseBillLocked(actor, billId);
            return null;
        });
    }

    private void dispenseBillLocked(UserPrincipal actor, Long billId) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Bill not found"));
        if (!BillRefs.PHARMACY.equals(bill.getReferenceType())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Not a pharmacy bill");
        }
        if (bill.getStatus() != BillStatus.PAID) {
            throw new ApiException(HttpStatus.PAYMENT_REQUIRED, "Pay-first: bill not paid");
        }
        User pharmacist = userRepository.findById(actor.userId())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User missing"));
        List<PharmacyLineFulfillment> linked = fulfillmentRepository.findByBill_Id(billId).stream()
                .filter(f -> f.getStatus() == PharmacyFulfillmentStatus.PENDING)
                .toList();
        if (linked.isEmpty()) {
            throw new ApiException(HttpStatus.CONFLICT, "Nothing to dispense for this bill");
        }
        Instant now = Instant.now();
        for (PharmacyLineFulfillment f : linked) {
            f.setStatus(PharmacyFulfillmentStatus.DISPENSED);
            f.setDispensedAt(now);
            f.setPharmacist(pharmacist);
            f.setUpdatedAt(now);
            fulfillmentRepository.save(f);
        }

        Long patientUserId = bill.getPatient().getId();
        notificationProducer.publish(NotificationEnvelope.create(
                NotificationType.PHARMACY_DISPENSED,
                patientUserId,
                "Medicines dispensed",
                "Your pharmacy order for bill #" + billId + " has been dispensed.",
                BillRefs.PHARMACY,
                billId));
    }
}
