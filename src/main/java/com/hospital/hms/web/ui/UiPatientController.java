package com.hospital.hms.web.ui;

import com.hospital.hms.auth.UserPrincipal;
import com.hospital.hms.config.RequiresMvcRole;
import com.hospital.hms.domain.enums.PaymentMode;
import com.hospital.hms.domain.enums.RoleCode;
import com.hospital.hms.repo.AppointmentRepository;
import com.hospital.hms.repo.BillRepository;
import com.hospital.hms.repo.PatientProfileRepository;
import com.hospital.hms.repo.PrescriptionRepository;
import com.hospital.hms.repo.UserRepository;
import com.hospital.hms.service.AppointmentService;
import com.hospital.hms.service.BillingService;
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
@RequestMapping("/app/patient")
@RequiresMvcRole(RoleCode.PATIENT)
public class UiPatientController {

    private final AppointmentRepository appointmentRepository;
    private final BillRepository billRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final UserRepository userRepository;
    private final AppointmentService appointmentService;
    private final BillingService billingService;

    public UiPatientController(AppointmentRepository appointmentRepository,
            BillRepository billRepository,
            PrescriptionRepository prescriptionRepository,
            PatientProfileRepository patientProfileRepository,
            UserRepository userRepository,
            AppointmentService appointmentService,
            BillingService billingService) {
        this.appointmentRepository = appointmentRepository;
        this.billRepository = billRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.patientProfileRepository = patientProfileRepository;
        this.userRepository = userRepository;
        this.appointmentService = appointmentService;
        this.billingService = billingService;
    }

    @GetMapping("/home")
    public String home(@RequestAttribute(HmsConstants.REQUEST_USER) UserPrincipal principal, Model model) {
        patientProfileRepository.findByUser_Id(principal.userId()).ifPresent(p -> model.addAttribute("mrn", p.getMrn()));
        model.addAttribute("appointments", appointmentRepository.findByPatient_Id(principal.userId()));
        model.addAttribute("bills", billRepository.findByPatient_IdOrderByCreatedAtDesc(principal.userId()));
        model.addAttribute("prescriptions", prescriptionRepository.findAllForPatient(principal.userId()));
        return "app/patient/home";
    }

    @PostMapping("/home/book")
    public String book(
            @RequestAttribute(HmsConstants.REQUEST_USER) UserPrincipal principal,
            @RequestParam String scheduledAtLocal,
            @RequestParam BigDecimal consultationFee,
            @RequestParam(required = false) String notes,
            RedirectAttributes ra) {
        try {
            var patient = userRepository.findById(principal.userId()).orElseThrow();
            var when = HmsWebTime.parseDatetimeLocalIst(scheduledAtLocal);
            appointmentService.bookOnline(patient, patient, when, consultationFee, notes);
            ra.addFlashAttribute(
                    "flashSuccess",
                    "Appointment booked. If a fee applies, pay from the Bills section or the Pay button below.");
        } catch (ApiException e) {
            ra.addFlashAttribute("flashError", e.getMessage());
        }
        return "redirect:/app/patient/home";
    }

    @PostMapping("/home/pay-appointment")
    public String payAppointment(
            @RequestAttribute(HmsConstants.REQUEST_USER) UserPrincipal principal,
            @RequestParam Long appointmentId,
            RedirectAttributes ra) {
        try {
            appointmentService.payAppointmentBill(principal, appointmentId, PaymentMode.ONLINE, "patient-portal", null);
            ra.addFlashAttribute("flashSuccess", "Appointment fee paid. Visit is confirmed.");
        } catch (ApiException e) {
            ra.addFlashAttribute("flashError", e.getMessage());
        }
        return "redirect:/app/patient/home";
    }

    @PostMapping("/home/pay-bill")
    public String payBill(
            @RequestAttribute(HmsConstants.REQUEST_USER) UserPrincipal principal,
            @RequestParam Long billId,
            RedirectAttributes ra) {
        try {
            var bill = billRepository.findById(billId).orElseThrow();
            if (!bill.getPatient().getId().equals(principal.userId())) {
                ra.addFlashAttribute("flashError", "Not your bill");
                return "redirect:/app/patient/home";
            }
            billingService.payBill(
                    principal,
                    billId,
                    PaymentMode.ONLINE,
                    bill.getTotalAmount(),
                    "patient-portal",
                    null);
            ra.addFlashAttribute("flashSuccess", "Bill paid.");
        } catch (ApiException e) {
            ra.addFlashAttribute("flashError", e.getMessage());
        }
        return "redirect:/app/patient/home";
    }
}
