package com.hospital.hms.web.ui;

import com.hospital.hms.auth.UserPrincipal;
import com.hospital.hms.domain.User;
import com.hospital.hms.domain.enums.RoleCode;
import com.hospital.hms.notification.NotificationInboxStore;
import com.hospital.hms.repo.UserRepository;
import com.hospital.hms.support.HmsConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(basePackages = "com.hospital.hms.web.ui")
public class MvcGlobalModelAdvice {

    private final UserRepository userRepository;
    private final NotificationInboxStore notificationInboxStore;

    public MvcGlobalModelAdvice(UserRepository userRepository, NotificationInboxStore notificationInboxStore) {
        this.userRepository = userRepository;
        this.notificationInboxStore = notificationInboxStore;
    }

    @ModelAttribute
    public void enrichMvcModel(HttpServletRequest request, Model model) {
        Object raw = request.getAttribute(HmsConstants.REQUEST_USER);
        if (!(raw instanceof UserPrincipal principal)) {
            return;
        }
        model.addAttribute("principal", principal);
        Map<RoleCode, Boolean> roleFlags = new EnumMap<>(RoleCode.class);
        for (RoleCode rc : RoleCode.values()) {
            roleFlags.put(rc, principal.roles().contains(rc));
        }
        model.addAttribute("roleFlags", roleFlags);
        model.addAttribute("isAdmin", principal.isAdmin());

        userRepository.findById(principal.userId()).ifPresent(u -> {
            model.addAttribute("userFullName", u.getFullName());
            model.addAttribute("userEmail", u.getEmail());
        });

        model.addAttribute("navPatient", principal.roles().contains(RoleCode.PATIENT));
        model.addAttribute("navReception", principal.roles().contains(RoleCode.RECEPTION));
        model.addAttribute("navClinical", principal.roles().contains(RoleCode.DOCTOR)
                || principal.roles().contains(RoleCode.ASSISTANT));
        model.addAttribute("navPharmacy", principal.roles().contains(RoleCode.PHARMACY));
        model.addAttribute("navAdmin", principal.roles().contains(RoleCode.ADMIN));
        model.addAttribute("navHr", principal.roles().contains(RoleCode.HR));
        model.addAttribute("navProcurement", principal.roles().contains(RoleCode.PROCUREMENT));
        model.addAttribute("navFinance", principal.roles().contains(RoleCode.HR)
                || principal.roles().contains(RoleCode.ADMIN));
        model.addAttribute("navFacility", principal.roles().contains(RoleCode.RECEPTION)
                || principal.roles().contains(RoleCode.NURSE));
        model.addAttribute("navCompliance", principal.roles().contains(RoleCode.NURSE)
                || principal.roles().contains(RoleCode.ADMIN));
        model.addAttribute("navHrStaff", principal.roles().contains(RoleCode.DOCTOR)
                || principal.roles().contains(RoleCode.NURSE)
                || principal.roles().contains(RoleCode.RECEPTION)
                || principal.roles().contains(RoleCode.PHARMACY)
                || principal.roles().contains(RoleCode.ASSISTANT)
                || principal.roles().contains(RoleCode.CANTEEN_STAFF)
                || principal.roles().contains(RoleCode.HR)
                || principal.roles().contains(RoleCode.PROCUREMENT));

        model.addAttribute("notificationCount", notificationInboxStore.size(principal.userId()));
    }
}
