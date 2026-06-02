package com.hospital.hms.web.ui;

import com.hospital.hms.auth.UserPrincipal;
import com.hospital.hms.config.RequiresMvcRole;
import com.hospital.hms.domain.enums.BookingChannel;
import com.hospital.hms.domain.enums.RoleCode;
import com.hospital.hms.repo.PatientProfileRepository;
import com.hospital.hms.repo.UserRepository;
import com.hospital.hms.service.AppointmentService;
import com.hospital.hms.support.HmsConstants;
import com.hospital.hms.support.HmsWebTime;
import com.hospital.hms.web.error.ApiException;
import java.math.BigDecimal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/app/reception")
@RequiresMvcRole(RoleCode.RECEPTION)
public class UiReceptionController {

    private final PatientProfileRepository patientProfileRepository;
    private final UserRepository userRepository;
    private final AppointmentService appointmentService;

    public UiReceptionController(
            PatientProfileRepository patientProfileRepository,
            UserRepository userRepository,
            AppointmentService appointmentService) {
        this.patientProfileRepository = patientProfileRepository;
        this.userRepository = userRepository;
        this.appointmentService = appointmentService;
    }

    @GetMapping("/desk")
    public String desk(Model model) {
        model.addAttribute("profiles", patientProfileRepository.findAll());
        model.addAttribute("channels", BookingChannel.values());
        return "app/reception/desk";
    }

    @PostMapping("/desk/book")
    public String book(
            @RequestAttribute(HmsConstants.REQUEST_USER) UserPrincipal principal,
            @RequestParam Long patientUserId,
            @RequestParam String scheduledAtLocal,
            @RequestParam String bookingChannel,
            @RequestParam BigDecimal consultationFee,
            @RequestParam(required = false) String notes,
            @RequestParam(defaultValue = "false") boolean offlinePaidImmediate,
            RedirectAttributes ra) {
        try {
            var booker = userRepository.findById(principal.userId()).orElseThrow();
            var patient = userRepository.findById(patientUserId).orElseThrow();
            var ch = BookingChannel.valueOf(bookingChannel);
            var when = HmsWebTime.parseDatetimeLocalIst(scheduledAtLocal);
            var appt = appointmentService.bookStaff(
                    patient, booker, ch, when, consultationFee, notes, offlinePaidImmediate, principal.userId(), principal);
            ra.addFlashAttribute(
                    "flashSuccess",
                    "Appointment #" + appt.getId() + " — " + appt.getStatus().name());
        } catch (ApiException e) {
            ra.addFlashAttribute("flashError", e.getMessage());
        }
        return "redirect:/app/reception/desk";
    }

    @PostMapping("/desk/cancel")
    public String cancel(
            @RequestAttribute(HmsConstants.REQUEST_USER) UserPrincipal principal,
            @RequestParam Long appointmentId,
            RedirectAttributes ra) {
        try {
            appointmentService.cancelAppointment(appointmentId, principal);
            ra.addFlashAttribute("flashSuccess", "Appointment cancelled.");
        } catch (ApiException e) {
            ra.addFlashAttribute("flashError", e.getMessage());
        }
        return "redirect:/app/reception/desk";
    }
}
