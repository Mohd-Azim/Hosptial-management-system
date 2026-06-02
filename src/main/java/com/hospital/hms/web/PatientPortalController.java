package com.hospital.hms.web;

import com.hospital.hms.domain.enums.RoleCode;
import com.hospital.hms.repo.AppointmentRepository;
import com.hospital.hms.repo.BillRepository;
import com.hospital.hms.repo.PatientDocumentRepository;
import com.hospital.hms.repo.PrescriptionLineRepository;
import com.hospital.hms.repo.PrescriptionRepository;
import com.hospital.hms.repo.UserRepository;
import com.hospital.hms.service.AppointmentService;
import com.hospital.hms.config.RequiresRole;
import com.hospital.hms.web.dto.BookAppointmentRequest;
import com.hospital.hms.web.dto.PayBillRequest;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/patient")
public class PatientPortalController {

    private final UserRepository userRepository;
    private final AppointmentService appointmentService;
    private final AppointmentRepository appointmentRepository;
    private final BillRepository billRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionLineRepository prescriptionLineRepository;
    private final PatientDocumentRepository patientDocumentRepository;

    public PatientPortalController(UserRepository userRepository,
            AppointmentService appointmentService,
            AppointmentRepository appointmentRepository,
            BillRepository billRepository,
            PrescriptionRepository prescriptionRepository,
            PrescriptionLineRepository prescriptionLineRepository,
            PatientDocumentRepository patientDocumentRepository) {
        this.userRepository = userRepository;
        this.appointmentService = appointmentService;
        this.appointmentRepository = appointmentRepository;
        this.billRepository = billRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.prescriptionLineRepository = prescriptionLineRepository;
        this.patientDocumentRepository = patientDocumentRepository;
    }

    @RequiresRole(RoleCode.PATIENT)
    @PostMapping("/appointments")
    public ResponseEntity<Map<String, Object>> book(@RequestBody BookAppointmentRequest body,
            HttpServletRequest request) {
        var p = WebRequests.principal(request);
        var patient = userRepository.findById(p.userId()).orElseThrow();
        Instant when = Instant.parse(body.scheduledAtIso());
        var appt = appointmentService.bookOnline(patient, patient, when,
                body.consultationFee() != null ? body.consultationFee() : java.math.BigDecimal.ZERO,
                body.notes());
        return ResponseEntity.ok(Map.of(
                "appointmentId", appt.getId(),
                "status", appt.getStatus().name()));
    }

    @RequiresRole(RoleCode.PATIENT)
    @PostMapping("/appointments/{id}/pay")
    public ResponseEntity<Void> payAppointment(@PathVariable Long id,
            @RequestBody PayBillRequest body,
            HttpServletRequest request) {
        var principal = WebRequests.principal(request);
        appointmentService.payAppointmentBill(principal, id, body.mode(), body.externalRef(),
                body.receivedByUserId());
        return ResponseEntity.ok().build();
    }

    @RequiresRole(RoleCode.PATIENT)
    @GetMapping("/appointments")
    public ResponseEntity<List<Map<String, Object>>> myAppointments(HttpServletRequest request) {
        var p = WebRequests.principal(request);
        return ResponseEntity.ok(
                appointmentRepository.findByPatient_Id(p.userId()).stream().map(a -> Map.<String, Object>of(
                        "id", a.getId(),
                        "scheduledAt", a.getScheduledAt().toString(),
                        "status", a.getStatus().name(),
                        "channel", a.getBookingChannel().name())).collect(Collectors.toList()));
    }

    @RequiresRole(RoleCode.PATIENT)
    @GetMapping("/prescriptions")
    public ResponseEntity<List<Map<String, Object>>> prescriptions(HttpServletRequest request) {
        var p = WebRequests.principal(request);
        var list = prescriptionRepository.findAllForPatient(p.userId()).stream().map(rx -> {
            var lines = prescriptionLineRepository.findByPrescription_Id(rx.getId()).stream()
                    .map(l -> Map.<String, Object>of(
                            "medicine", l.getMedicineName(),
                            "dosage", l.getDosage(),
                            "frequency", l.getFrequency(),
                            "durationDays", l.getDurationDays(),
                            "unitPrice", l.getUnitPrice()))
                    .collect(Collectors.toList());
            return Map.<String, Object>of(
                    "prescriptionId", rx.getId(),
                    "printedAt", rx.getPrintedAt() != null ? rx.getPrintedAt().toString() : "",
                    "pharmacyOptIn", rx.isPharmacyOptIn(),
                    "lines", lines);
        }).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @RequiresRole(RoleCode.PATIENT)
    @GetMapping("/bills")
    public ResponseEntity<List<Map<String, Object>>> bills(HttpServletRequest request) {
        var p = WebRequests.principal(request);
        var list = billRepository.findByPatient_IdOrderByCreatedAtDesc(p.userId()).stream()
                .map(b -> Map.<String, Object>of(
                        "billId", b.getId(),
                        "referenceType", b.getReferenceType(),
                        "total", b.getTotalAmount(),
                        "status", b.getStatus().name(),
                        "gstInvoice", b.isGstInvoice()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @RequiresRole(RoleCode.PATIENT)
    @GetMapping("/documents")
    public ResponseEntity<List<Map<String, Object>>> documents(HttpServletRequest request) {
        var p = WebRequests.principal(request);
        var list = patientDocumentRepository.findByPatient_IdOrderByCreatedAtDesc(p.userId()).stream()
                .map(d -> Map.<String, Object>of(
                        "id", d.getId(),
                        "docType", d.getDocType(),
                        "title", d.getTitle(),
                        "uri", d.getStorageUri()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }
}
