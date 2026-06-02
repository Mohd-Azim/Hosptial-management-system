package com.hospital.hms.web.ui;

import com.hospital.hms.auth.UserPrincipal;
import com.hospital.hms.config.RequiresMvcRole;
import com.hospital.hms.domain.Bill;
import com.hospital.hms.domain.enums.BillStatus;
import com.hospital.hms.domain.enums.PaymentMode;
import com.hospital.hms.domain.enums.RoleCode;
import com.hospital.hms.repo.BillRepository;
import com.hospital.hms.service.BillingService;
import com.hospital.hms.service.PharmacyService;
import com.hospital.hms.support.BillRefs;
import com.hospital.hms.support.HmsConstants;
import com.hospital.hms.web.dto.PharmacyPendingRow;
import com.hospital.hms.web.error.ApiException;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/app/pharmacy")
@RequiresMvcRole(RoleCode.PHARMACY)
public class UiPharmacyController {

    private final PharmacyService pharmacyService;
    private final BillRepository billRepository;
    private final BillingService billingService;

    public UiPharmacyController(
            PharmacyService pharmacyService, BillRepository billRepository, BillingService billingService) {
        this.pharmacyService = pharmacyService;
        this.billRepository = billRepository;
        this.billingService = billingService;
    }

    @GetMapping("/pending")
    public String pending(Model model) {
        List<PharmacyPendingRow> rows = pharmacyService.pending().stream()
                .map(f -> new PharmacyPendingRow(
                        f.getId(),
                        f.getPrescriptionLine().getId(),
                        f.getPrescriptionLine().getMedicineName(),
                        f.getPrescriptionLine().getUnitPrice(),
                        f.getPrescriptionLine()
                                .getPrescription()
                                .getVisit()
                                .getAppointment()
                                .getPatient()
                                .getId(),
                        f.getPrescriptionLine()
                                .getPrescription()
                                .getVisit()
                                .getAppointment()
                                .getPatient()
                                .getFullName()))
                .toList();
        model.addAttribute("rows", rows);
        List<Bill> openPharmacyBills =
                billRepository.findByReferenceTypeAndStatusOrderByCreatedAtDesc(BillRefs.PHARMACY, BillStatus.OPEN);
        model.addAttribute("openPharmacyBills", openPharmacyBills);
        return "app/pharmacy/pending";
    }

    @PostMapping("/pending/create-bill")
    public String createBill(
            @RequestAttribute(HmsConstants.REQUEST_USER) UserPrincipal principal,
            @RequestParam(value = "fid", required = false) List<Long> fids,
            RedirectAttributes ra) {
        if (fids == null || fids.isEmpty()) {
            ra.addFlashAttribute("flashError", "Select at least one fulfilment line.");
            return "redirect:/app/pharmacy/pending";
        }
        try {
            Bill bill = pharmacyService.createBillForFulfillments(principal, fids);
            ra.addFlashAttribute(
                    "flashSuccess",
                    "Bill #" + bill.getId() + " created (" + bill.getTotalAmount() + "). Collect payment, then dispense.");
        } catch (ApiException e) {
            ra.addFlashAttribute("flashError", e.getMessage());
        }
        return "redirect:/app/pharmacy/pending";
    }

    @PostMapping("/pending/pay-bill")
    public String payBill(
            @RequestAttribute(HmsConstants.REQUEST_USER) UserPrincipal principal,
            @RequestParam Long billId,
            RedirectAttributes ra) {
        try {
            Bill b = billRepository.findById(billId).orElseThrow();
            if (!BillRefs.PHARMACY.equals(b.getReferenceType())) {
                ra.addFlashAttribute("flashError", "Not a pharmacy bill");
                return "redirect:/app/pharmacy/pending";
            }
            billingService.payBill(
                    principal, billId, PaymentMode.OFFLINE, b.getTotalAmount(), "pharmacy-counter", principal.userId());
            ra.addFlashAttribute("flashSuccess", "Bill #" + billId + " marked paid.");
        } catch (ApiException e) {
            ra.addFlashAttribute("flashError", e.getMessage());
        }
        return "redirect:/app/pharmacy/pending";
    }

    @PostMapping("/pending/dispense")
    public String dispense(
            @RequestAttribute(HmsConstants.REQUEST_USER) UserPrincipal principal,
            @RequestParam Long billId,
            RedirectAttributes ra) {
        try {
            pharmacyService.dispenseBill(principal, billId);
            ra.addFlashAttribute("flashSuccess", "Bill #" + billId + " dispensed.");
        } catch (ApiException e) {
            ra.addFlashAttribute("flashError", e.getMessage());
        }
        return "redirect:/app/pharmacy/pending";
    }
}
