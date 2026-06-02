package com.hospital.hms.service;

import com.hospital.hms.auth.UserPrincipal;
import com.hospital.hms.domain.Appointment;
import com.hospital.hms.domain.User;
import com.hospital.hms.domain.enums.AppointmentStatus;
import com.hospital.hms.domain.enums.BookingChannel;
import com.hospital.hms.domain.enums.PaymentMode;
import com.hospital.hms.domain.enums.RoleCode;
import com.hospital.hms.repo.AppointmentRepository;
import com.hospital.hms.repo.BillRepository;
import com.hospital.hms.support.BillRefs;
import com.hospital.hms.web.error.ApiException;
import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final BillRepository billRepository;
    private final BillingService billingService;

    public AppointmentService(AppointmentRepository appointmentRepository,
            BillRepository billRepository,
            BillingService billingService) {
        this.appointmentRepository = appointmentRepository;
        this.billRepository = billRepository;
        this.billingService = billingService;
    }

    @Transactional
    public Appointment bookOnline(User patient, User booker, Instant scheduledAt, BigDecimal consultationFee, String notes) {
        return book(patient, booker, BookingChannel.ONLINE, scheduledAt, consultationFee, notes, false, null);
    }

    @Transactional
    public Appointment bookStaff(User patient, User booker, BookingChannel channel,
            Instant scheduledAt, BigDecimal consultationFee, String notes,
            boolean offlinePaidImmediate, Long receptionUserId, UserPrincipal actor) {
        if (!actor.roles().contains(RoleCode.RECEPTION) && !actor.isAdmin()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Reception or admin only");
        }
        return book(patient, booker, channel, scheduledAt, consultationFee, notes, offlinePaidImmediate, receptionUserId);
    }

    private Appointment book(User patient, User booker, BookingChannel channel,
            Instant scheduledAt, BigDecimal consultationFee, String notes,
            boolean offlinePaidImmediate, Long receivedByUserId) {
        Appointment a = new Appointment();
        a.setPatient(patient);
        a.setBookedBy(booker);
        a.setScheduledAt(scheduledAt);
        a.setBookingChannel(channel);
        a.setConsultationFee(consultationFee != null ? consultationFee : BigDecimal.ZERO);
        a.setNotes(notes);
        a.setCreatedAt(Instant.now());
        a.setUpdatedAt(Instant.now());

        BigDecimal fee = a.getConsultationFee();
        if (fee.compareTo(BigDecimal.ZERO) <= 0) {
            a.setStatus(AppointmentStatus.CONFIRMED);
            appointmentRepository.save(a);
            return a;
        }

        if (offlinePaidImmediate) {
            a.setStatus(AppointmentStatus.CONFIRMED);
        } else {
            a.setStatus(AppointmentStatus.PENDING_PAYMENT);
        }
        appointmentRepository.save(a);

        var bill = billingService.createBill(patient, BillRefs.APPOINTMENT, a.getId(),
                fee, BigDecimal.ZERO, false);

        if (offlinePaidImmediate) {
            UserPrincipal cashier = new UserPrincipal(receivedByUserId, "", java.util.Set.of(RoleCode.RECEPTION));
            billingService.payBill(cashier, bill.getId(), PaymentMode.OFFLINE, bill.getTotalAmount(),
                    "counter", receivedByUserId);
        }
        return appointmentRepository.findById(a.getId()).orElseThrow();
    }

    @Transactional
    public Appointment cancelAppointment(Long appointmentId, UserPrincipal actor) {
        Appointment a = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Appointment not found"));
        boolean ownerPatient = actor.userId().equals(a.getPatient().getId()) && actor.roles().contains(RoleCode.PATIENT);
        boolean reception = actor.roles().contains(RoleCode.RECEPTION) || actor.isAdmin();
        if (!ownerPatient && !reception) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Cannot cancel");
        }
        if (a.getStatus() == AppointmentStatus.COMPLETED) {
            throw new ApiException(HttpStatus.CONFLICT, "Already completed");
        }
        a.setStatus(AppointmentStatus.CANCELLED);
        a.setUpdatedAt(Instant.now());
        return appointmentRepository.save(a);
    }

    @Transactional
    public void payAppointmentBill(UserPrincipal payer, Long appointmentId, PaymentMode mode,
            String externalRef, Long receivedByUserId) {
        Appointment a = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Appointment not found"));
        if (a.getStatus() != AppointmentStatus.PENDING_PAYMENT) {
            throw new ApiException(HttpStatus.CONFLICT, "Appointment not awaiting payment");
        }
        var bill = billRepository.findByReferenceTypeAndReferenceId(BillRefs.APPOINTMENT, appointmentId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Bill not found for appointment"));
        billingService.payBill(payer, bill.getId(), mode, bill.getTotalAmount(), externalRef, receivedByUserId);
        a.setStatus(AppointmentStatus.CONFIRMED);
        a.setUpdatedAt(Instant.now());
        appointmentRepository.save(a);
    }
}
