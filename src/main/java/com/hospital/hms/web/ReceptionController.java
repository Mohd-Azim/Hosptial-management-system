package com.hospital.hms.web;

import com.hospital.hms.domain.enums.BookingChannel;
import com.hospital.hms.domain.enums.RoleCode;
import com.hospital.hms.repo.UserRepository;
import com.hospital.hms.service.AppointmentService;
import com.hospital.hms.config.RequiresRole;
import com.hospital.hms.web.dto.ReceptionBookRequest;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reception")
public class ReceptionController {

    private final UserRepository userRepository;
    private final AppointmentService appointmentService;

    public ReceptionController(UserRepository userRepository, AppointmentService appointmentService) {
        this.userRepository = userRepository;
        this.appointmentService = appointmentService;
    }

    @RequiresRole({RoleCode.RECEPTION, RoleCode.ADMIN})
    @PostMapping("/appointments")
    public ResponseEntity<Map<String, Object>> bookForPatient(@RequestBody ReceptionBookRequest body,
            HttpServletRequest request) {
        var actor = WebRequests.principal(request);
        var patient = userRepository.findById(body.patientUserId()).orElseThrow();
        var booker = userRepository.findById(actor.userId()).orElseThrow();
        BookingChannel ch = BookingChannel.valueOf(body.bookingChannel());
        Instant when = Instant.parse(body.scheduledAtIso());
        var appt = appointmentService.bookStaff(patient, booker, ch, when,
                body.consultationFee() != null ? body.consultationFee() : java.math.BigDecimal.ZERO,
                body.notes(), body.offlinePaidImmediate(), actor.userId(), actor);
        return ResponseEntity.ok(Map.of("appointmentId", appt.getId(), "status", appt.getStatus().name()));
    }

    @RequiresRole({RoleCode.RECEPTION, RoleCode.ADMIN})
    @PostMapping("/appointments/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancel(@PathVariable Long id, HttpServletRequest request) {
        var actor = WebRequests.principal(request);
        var appt = appointmentService.cancelAppointment(id, actor);
        return ResponseEntity.ok(Map.of("appointmentId", appt.getId(), "status", appt.getStatus().name()));
    }
}
