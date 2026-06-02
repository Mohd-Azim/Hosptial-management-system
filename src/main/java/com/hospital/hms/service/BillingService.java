package com.hospital.hms.service;

import com.hospital.hms.auth.UserPrincipal;
import com.hospital.hms.domain.Bill;
import com.hospital.hms.domain.Payment;
import com.hospital.hms.domain.User;
import com.hospital.hms.domain.enums.BillStatus;
import com.hospital.hms.domain.enums.PaymentMode;
import com.hospital.hms.domain.enums.RoleCode;
import com.hospital.hms.notification.NotificationEnvelope;
import com.hospital.hms.notification.NotificationProducer;
import com.hospital.hms.notification.NotificationType;
import com.hospital.hms.pharmacylock.PharmacyOperationLock;
import com.hospital.hms.repo.BillRepository;
import com.hospital.hms.repo.PaymentRepository;
import com.hospital.hms.repo.UserRepository;
import com.hospital.hms.support.BillRefs;
import com.hospital.hms.web.error.ApiException;
import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BillingService {

    private final BillRepository billRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final PharmacyOperationLock pharmacyOperationLock;
    private final NotificationProducer notificationProducer;

    public BillingService(BillRepository billRepository,
            PaymentRepository paymentRepository,
            UserRepository userRepository,
            PharmacyOperationLock pharmacyOperationLock,
            NotificationProducer notificationProducer) {
        this.billRepository = billRepository;
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
        this.pharmacyOperationLock = pharmacyOperationLock;
        this.notificationProducer = notificationProducer;
    }

    @Transactional
    public Bill createBill(User patient, String referenceType, Long referenceId,
            BigDecimal amount, BigDecimal taxAmount, boolean gstInvoice) {
        Bill bill = new Bill();
        bill.setPatient(patient);
        bill.setReferenceType(referenceType);
        bill.setReferenceId(referenceId);
        bill.setAmount(amount);
        bill.setTaxAmount(taxAmount != null ? taxAmount : BigDecimal.ZERO);
        bill.setTotalAmount(amount.add(bill.getTaxAmount()));
        bill.setGstInvoice(gstInvoice);
        bill.setStatus(BillStatus.OPEN);
        bill.setCreatedAt(Instant.now());
        bill.setUpdatedAt(Instant.now());
        return billRepository.save(bill);
    }

    @Transactional(readOnly = true)
    public void assertBillPaid(Long billId) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Bill not found"));
        if (bill.getStatus() != BillStatus.PAID) {
            throw new ApiException(HttpStatus.PAYMENT_REQUIRED, "Payment required (pay-first policy)");
        }
    }

    @Transactional
    public Payment payBill(UserPrincipal principal, Long billId, PaymentMode mode,
            BigDecimal amount, String externalRef, Long receivedByUserId) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Bill not found"));
        if (bill.getStatus() != BillStatus.OPEN) {
            throw new ApiException(HttpStatus.CONFLICT, "Bill not payable");
        }
        Long patientId = bill.getPatient().getId();
        boolean selfPatient = principal.userId().equals(patientId) && principal.roles().contains(RoleCode.PATIENT);
        boolean staffCashier = principal.isAdmin()
                || principal.roles().contains(RoleCode.RECEPTION)
                || principal.roles().contains(RoleCode.PHARMACY)
                || principal.roles().contains(RoleCode.CANTEEN_STAFF)
                || principal.roles().contains(RoleCode.NURSE);
        if (!selfPatient && !staffCashier) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Cannot pay this bill");
        }
        if (amount.compareTo(bill.getTotalAmount()) != 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Amount must match bill total exactly");
        }

        if (BillRefs.PHARMACY.equals(bill.getReferenceType())) {
            return pharmacyOperationLock.withBillLock(billId, () -> payBillLocked(
                    principal, billId, mode, amount, externalRef, receivedByUserId, selfPatient));
        }
        return payBillLocked(principal, billId, mode, amount, externalRef, receivedByUserId, selfPatient);
    }

    private Payment payBillLocked(UserPrincipal principal, Long billId, PaymentMode mode,
            BigDecimal amount, String externalRef, Long receivedByUserId, boolean selfPatient) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Bill not found"));
        if (bill.getStatus() != BillStatus.OPEN) {
            throw new ApiException(HttpStatus.CONFLICT, "Bill not payable");
        }
        if (amount.compareTo(bill.getTotalAmount()) != 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Amount must match bill total exactly");
        }

        Payment p = new Payment();
        p.setBill(bill);
        p.setMode(mode);
        p.setAmount(amount);
        p.setExternalRef(externalRef);
        if (receivedByUserId != null) {
            p.setReceivedBy(userRepository.findById(receivedByUserId).orElse(null));
        } else if (!selfPatient) {
            p.setReceivedBy(userRepository.findById(principal.userId()).orElse(null));
        }
        p.setCreatedAt(Instant.now());
        paymentRepository.save(p);

        bill.setStatus(BillStatus.PAID);
        bill.setUpdatedAt(Instant.now());
        billRepository.save(bill);

        if (BillRefs.PHARMACY.equals(bill.getReferenceType())) {
            Long patientUserId = bill.getPatient().getId();
            notificationProducer.publish(NotificationEnvelope.create(
                    NotificationType.PHARMACY_PAYMENT_CONFIRMED,
                    patientUserId,
                    "Pharmacy payment received",
                    "Your pharmacy bill #" + billId + " is paid.",
                    BillRefs.PHARMACY,
                    billId));
        }

        return p;
    }
}
