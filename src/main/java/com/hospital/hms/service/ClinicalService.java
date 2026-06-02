package com.hospital.hms.service;

import com.hospital.hms.auth.UserPrincipal;
import com.hospital.hms.domain.Appointment;
import com.hospital.hms.domain.PharmacyLineFulfillment;
import com.hospital.hms.domain.Prescription;
import com.hospital.hms.domain.PrescriptionLine;
import com.hospital.hms.domain.User;
import com.hospital.hms.domain.Visit;
import com.hospital.hms.domain.enums.AppointmentStatus;
import com.hospital.hms.domain.enums.PharmacyFulfillmentStatus;
import com.hospital.hms.domain.enums.RoleCode;
import com.hospital.hms.domain.enums.VisitStatus;
import com.hospital.hms.notification.NotificationEnvelope;
import com.hospital.hms.notification.NotificationProducer;
import com.hospital.hms.notification.NotificationType;
import com.hospital.hms.repo.AppointmentRepository;
import com.hospital.hms.repo.PharmacyLineFulfillmentRepository;
import com.hospital.hms.repo.PrescriptionLineRepository;
import com.hospital.hms.repo.PrescriptionRepository;
import com.hospital.hms.repo.UserRepository;
import com.hospital.hms.repo.VisitRepository;
import com.hospital.hms.support.BillRefs;
import com.hospital.hms.web.dto.PrescriptionLineRequest;
import com.hospital.hms.web.error.ApiException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClinicalService {

    private final AppointmentRepository appointmentRepository;
    private final VisitRepository visitRepository;
    private final UserRepository userRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionLineRepository prescriptionLineRepository;
    private final PharmacyLineFulfillmentRepository pharmacyLineFulfillmentRepository;
    private final NotificationProducer notificationProducer;
    private final UserDisplayService userDisplayService;

    public ClinicalService(AppointmentRepository appointmentRepository,
            VisitRepository visitRepository,
            UserRepository userRepository,
            PrescriptionRepository prescriptionRepository,
            PrescriptionLineRepository prescriptionLineRepository,
            PharmacyLineFulfillmentRepository pharmacyLineFulfillmentRepository,
            NotificationProducer notificationProducer,
            UserDisplayService userDisplayService) {
        this.appointmentRepository = appointmentRepository;
        this.visitRepository = visitRepository;
        this.userRepository = userRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.prescriptionLineRepository = prescriptionLineRepository;
        this.pharmacyLineFulfillmentRepository = pharmacyLineFulfillmentRepository;
        this.notificationProducer = notificationProducer;
        this.userDisplayService = userDisplayService;
    }

    @Transactional(readOnly = true)
    public List<Appointment> todayConfirmed(LocalDate day) {
        return appointmentRepository.findByStatusAndScheduledLocalDateOrderByScheduledAtAsc(
                AppointmentStatus.CONFIRMED, day);
    }

    @Transactional(readOnly = true)
    public List<Appointment> todayConfirmed(Instant start, Instant end) {
        return appointmentRepository.findByStatusAndScheduledAtBetweenOrderByScheduledAtAsc(
                AppointmentStatus.CONFIRMED, start, end);
    }

    @Transactional
    public Visit checkIn(Long appointmentId, Long doctorUserId, Long assistantUserId, UserPrincipal actor) {
        if (!actor.hasAny(RoleCode.DOCTOR, RoleCode.ASSISTANT, RoleCode.ADMIN)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Clinical staff only");
        }
        if (!actor.isAdmin() && actor.roles().contains(RoleCode.DOCTOR) && !actor.userId().equals(doctorUserId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can only check in as yourself (doctor)");
        }
        Appointment ap = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Appointment not found"));
        if (ap.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new ApiException(HttpStatus.CONFLICT, "Appointment must be confirmed (paid)");
        }
        if (visitRepository.findByAppointment_Id(appointmentId).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "Visit already started");
        }
        User doctor = userRepository.findById(doctorUserId)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Doctor not found"));
        Visit v = new Visit();
        v.setAppointment(ap);
        v.setDoctor(doctor);
        if (assistantUserId != null) {
            v.setAssistant(userRepository.findById(assistantUserId).orElse(null));
        }
        v.setStatus(VisitStatus.CHECKED_IN);
        v.setCheckedInAt(Instant.now());
        v.setCreatedAt(Instant.now());
        v.setUpdatedAt(Instant.now());
        Visit saved = visitRepository.save(v);

        Long patientUserId = ap.getPatient().getId();
        String patientLabel = userDisplayService.fullName(patientUserId);
        notificationProducer.publish(NotificationEnvelope.create(
                NotificationType.PATIENT_ASSIGNED_TO_DOCTOR,
                doctorUserId,
                "Patient assigned to you",
                patientLabel + " checked in for appointment #" + appointmentId,
                "APPOINTMENT",
                appointmentId));
        notificationProducer.publish(NotificationEnvelope.create(
                NotificationType.APPOINTMENT_CHECKIN_PATIENT,
                patientUserId,
                "Checked in",
                "You are checked in. Please wait for your consultation.",
                "APPOINTMENT",
                appointmentId));

        return saved;
    }

    @Transactional
    public Prescription completeVisit(Long appointmentId, Long doctorUserId, Long assistantUserId,
            boolean pharmacyOptIn, String instructions, List<PrescriptionLineRequest> lines, UserPrincipal actor) {
        if (!actor.hasAny(RoleCode.DOCTOR, RoleCode.ASSISTANT, RoleCode.ADMIN)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Clinical staff only");
        }
        if (!actor.isAdmin() && actor.roles().contains(RoleCode.DOCTOR) && !actor.userId().equals(doctorUserId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Prescription must be issued by logged-in doctor");
        }
        Appointment ap = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Appointment not found"));
        Visit v = visitRepository.findByAppointment_Id(appointmentId)
                .orElseThrow(() -> new ApiException(HttpStatus.CONFLICT, "Check-in required first"));
        if (!v.getDoctor().getId().equals(doctorUserId) && !actor.isAdmin()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Doctor mismatch");
        }
        v.setStatus(VisitStatus.COMPLETED);
        v.setCompletedAt(Instant.now());
        v.setUpdatedAt(Instant.now());
        if (assistantUserId != null) {
            v.setAssistant(userRepository.findById(assistantUserId).orElse(null));
        }
        visitRepository.save(v);

        Prescription rx = new Prescription();
        rx.setVisit(v);
        rx.setInstructions(instructions);
        rx.setPharmacyOptIn(pharmacyOptIn);
        rx.setPharmacyOptInAt(pharmacyOptIn ? Instant.now() : null);
        rx.setPrintedAt(Instant.now());
        rx.setCreatedAt(Instant.now());
        rx.setUpdatedAt(Instant.now());
        prescriptionRepository.save(rx);

        for (PrescriptionLineRequest lr : lines) {
            PrescriptionLine pl = new PrescriptionLine();
            pl.setPrescription(rx);
            pl.setMedicineName(lr.medicineName());
            pl.setDosage(lr.dosage());
            pl.setFrequency(lr.frequency());
            pl.setDurationDays(lr.durationDays());
            pl.setUnitPrice(lr.unitPrice() != null ? lr.unitPrice() : BigDecimal.ZERO);
            pl.setCreatedAt(Instant.now());
            prescriptionLineRepository.save(pl);

            if (pharmacyOptIn) {
                PharmacyLineFulfillment f = new PharmacyLineFulfillment();
                f.setPrescriptionLine(pl);
                f.setStatus(PharmacyFulfillmentStatus.PENDING);
                f.setCreatedAt(Instant.now());
                f.setUpdatedAt(Instant.now());
                pharmacyLineFulfillmentRepository.save(f);
            }
        }

        ap.setStatus(AppointmentStatus.COMPLETED);
        ap.setUpdatedAt(Instant.now());
        appointmentRepository.save(ap);

        Prescription result = prescriptionRepository.findById(rx.getId()).orElseThrow();

        Long patientUserId = ap.getPatient().getId();
        String extra = pharmacyOptIn ? " Pharmacy will prepare your medicines." : "";
        notificationProducer.publish(NotificationEnvelope.create(
                NotificationType.PRESCRIPTION_READY,
                patientUserId,
                "Prescription ready",
                "Your prescription is available in the portal." + extra,
                "PRESCRIPTION",
                result.getId()));

        return result;
    }
}
