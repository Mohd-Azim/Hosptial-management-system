package com.hospital.hms.config;

import com.hospital.hms.domain.Bed;
import com.hospital.hms.domain.CanteenMenuItem;
import com.hospital.hms.domain.PatientProfile;
import com.hospital.hms.domain.Role;
import com.hospital.hms.domain.User;
import com.hospital.hms.domain.Ward;
import com.hospital.hms.domain.PayrollComponent;
import com.hospital.hms.domain.Vendor;
import com.hospital.hms.domain.enums.BedStatus;
import com.hospital.hms.domain.enums.PayrollComponentKind;
import com.hospital.hms.domain.enums.RoleCode;
import com.hospital.hms.domain.enums.VendorPartyType;
import com.hospital.hms.repo.BedRepository;
import com.hospital.hms.repo.CanteenMenuItemRepository;
import com.hospital.hms.repo.PatientProfileRepository;
import com.hospital.hms.repo.PayrollComponentRepository;
import com.hospital.hms.repo.RoleRepository;
import com.hospital.hms.repo.UserRepository;
import com.hospital.hms.repo.VendorRepository;
import com.hospital.hms.repo.WardRepository;
import com.hospital.hms.service.AuthService;
import com.hospital.hms.support.PasswordHasher;
import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DemoDataLoader implements ApplicationRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final PatientProfileRepository patientProfileRepository;
    private final WardRepository wardRepository;
    private final BedRepository bedRepository;
    private final CanteenMenuItemRepository canteenMenuItemRepository;
    private final PayrollComponentRepository payrollComponentRepository;
    private final VendorRepository vendorRepository;

    public DemoDataLoader(RoleRepository roleRepository,
            UserRepository userRepository,
            AuthService authService,
            PatientProfileRepository patientProfileRepository,
            WardRepository wardRepository,
            BedRepository bedRepository,
            CanteenMenuItemRepository canteenMenuItemRepository,
            PayrollComponentRepository payrollComponentRepository,
            VendorRepository vendorRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.authService = authService;
        this.patientProfileRepository = patientProfileRepository;
        this.wardRepository = wardRepository;
        this.bedRepository = bedRepository;
        this.canteenMenuItemRepository = canteenMenuItemRepository;
        this.payrollComponentRepository = payrollComponentRepository;
        this.vendorRepository = vendorRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedRoles();
        if (userRepository.findByUsername("admin").isEmpty()) {
            seedUsersAndProfiles();
        }
        seedWardsIfEmpty();
        seedCanteenMenuIfEmpty();
        seedPayrollComponentsIfEmpty();
        seedSampleVendorIfEmpty();
    }

    private void seedRoles() {
        for (RoleCode rc : RoleCode.values()) {
            roleRepository.findByName(rc.name()).orElseGet(() -> {
                Role r = new Role();
                r.setName(rc.name());
                r.setCreatedAt(Instant.now());
                r.setUpdatedAt(Instant.now());
                return roleRepository.save(r);
            });
        }
    }

    private void seedUsersAndProfiles() {
        User admin = saveUser("admin", "Admin User", "admin@hms.local", "demo123");
        authService.assignRole(admin, RoleCode.ADMIN);
        authService.assignRole(admin, RoleCode.HR);
        authService.assignRole(admin, RoleCode.PROCUREMENT);

        User reception = saveUser("reception1", "Reception Desk", "rec@hms.local", "demo123");
        authService.assignRole(reception, RoleCode.RECEPTION);

        User doctor = saveUser("doctor1", "Dr. Demo", "doc@hms.local", "demo123");
        authService.assignRole(doctor, RoleCode.DOCTOR);

        User assistant = saveUser("assistant1", "Clinical Assistant", "asst@hms.local", "demo123");
        authService.assignRole(assistant, RoleCode.ASSISTANT);

        User pharmacy = saveUser("pharmacy1", "Pharmacist", "rx@hms.local", "demo123");
        authService.assignRole(pharmacy, RoleCode.PHARMACY);

        User nurse = saveUser("nurse1", "Staff Nurse", "nurse@hms.local", "demo123");
        authService.assignRole(nurse, RoleCode.NURSE);

        User canteen = saveUser("canteen1", "Canteen Staff", "cant@hms.local", "demo123");
        authService.assignRole(canteen, RoleCode.CANTEEN_STAFF);

        User hr = saveUser("hr1", "HR Executive", "hr@hms.local", "demo123");
        authService.assignRole(hr, RoleCode.HR);

        User procurement = saveUser("procurement1", "Procurement Officer", "proc@hms.local", "demo123");
        authService.assignRole(procurement, RoleCode.PROCUREMENT);

        User patient = saveUser("patient1", "Demo Patient", "patient@hms.local", "demo123");
        authService.assignRole(patient, RoleCode.PATIENT);
        PatientProfile profile = new PatientProfile();
        profile.setUser(patient);
        profile.setMrn("MRN-SEED-" + patient.getId());
        patientProfileRepository.save(profile);
    }

    private User saveUser(String username, String fullName, String email, String plainPassword) {
        User u = new User();
        u.setUsername(username);
        u.setFullName(fullName);
        u.setEmail(email);
        u.setPhone("9999999999");
        u.setPasswordHash(PasswordHasher.hash(plainPassword));
        u.setActive(true);
        u.setSuspended(false);
        u.setCreatedAt(Instant.now());
        u.setUpdatedAt(Instant.now());
        return userRepository.save(u);
    }

    private void seedWardsIfEmpty() {
        if (wardRepository.count() > 0) {
            return;
        }
        Instant now = Instant.now();
        Ward emergency = persistWard("Emergency Bay", "EMERGENCY", now);
        Ward icu = persistWard("ICU", "ICU", now);
        Ward general = persistWard("General Ward", "GENERAL_WARD", now);
        Ward priv = persistWard("Private Rooms", "PRIVATE_ROOM", now);
        Ward suite = persistWard("Executive Suite", "SUITE", now);
        persistBed(emergency, "E-01", now);
        persistBed(icu, "ICU-01", now);
        persistBed(icu, "ICU-02", now);
        persistBed(general, "G-101", now);
        persistBed(general, "G-102", now);
        persistBed(priv, "P-201", now);
        persistBed(suite, "S-501", now);
    }

    private Ward persistWard(String name, String bedType, Instant now) {
        Ward w = new Ward();
        w.setName(name);
        w.setBedType(bedType);
        w.setCreatedAt(now);
        w.setUpdatedAt(now);
        return wardRepository.save(w);
    }

    private void persistBed(Ward ward, String code, Instant now) {
        Bed b = new Bed();
        b.setWard(ward);
        b.setBedCode(code);
        b.setStatus(BedStatus.AVAILABLE);
        b.setCreatedAt(now);
        b.setUpdatedAt(now);
        bedRepository.save(b);
    }

    private void seedCanteenMenuIfEmpty() {
        if (canteenMenuItemRepository.count() > 0) {
            return;
        }
        Instant now = Instant.now();
        persistMenuItem("Tea + biscuit", "veg", new BigDecimal("40"), now);
        persistMenuItem("South meals — mini", "veg", new BigDecimal("120"), now);
        persistMenuItem("Fresh juice", "beverage", new BigDecimal("90"), now);
    }

    private void persistMenuItem(String name, String desc, BigDecimal price, Instant now) {
        CanteenMenuItem i = new CanteenMenuItem();
        i.setName(name);
        i.setDescription(desc);
        i.setPrice(price);
        i.setAvailable(true);
        i.setCreatedAt(now);
        i.setUpdatedAt(now);
        canteenMenuItemRepository.save(i);
    }

    private void seedPayrollComponentsIfEmpty() {
        if (payrollComponentRepository.count() > 0) {
            return;
        }
        Instant now = Instant.now();
        persistPayrollComponent("BASIC", "Basic salary", PayrollComponentKind.EARNING, true, now);
        persistPayrollComponent("HRA", "House rent allowance", PayrollComponentKind.EARNING, true, now);
        persistPayrollComponent("SPECIAL_ALLOW", "Special allowance", PayrollComponentKind.EARNING, true, now);
        persistPayrollComponent("PF_EE", "Provident fund — employee", PayrollComponentKind.DEDUCTION, false, now);
        persistPayrollComponent("MED_INS_GRP", "Group medical benefit", PayrollComponentKind.BENEFIT, false, now);
    }

    private void persistPayrollComponent(String code, String name, PayrollComponentKind kind, boolean taxable,
            Instant now) {
        PayrollComponent c = new PayrollComponent();
        c.setCode(code);
        c.setName(name);
        c.setComponentKind(kind);
        c.setTaxable(taxable);
        c.setCreatedAt(now);
        c.setUpdatedAt(now);
        payrollComponentRepository.save(c);
    }

    private void seedSampleVendorIfEmpty() {
        if (vendorRepository.count() > 0) {
            return;
        }
        Instant now = Instant.now();
        Vendor v = new Vendor();
        v.setPartyType(VendorPartyType.SUPPLIER);
        v.setLegalName("Demo Medical Supplies Pvt Ltd");
        v.setTradeName("DemoMed");
        v.setGstin("29AAAAA0000A1Z5");
        v.setContactPerson("Vendor Desk");
        v.setPhone("9800000000");
        v.setEmail("accounts@demomed.example");
        v.setAddress("Industrial Area, Bengaluru");
        v.setPaymentTermsDays(30);
        v.setActive(true);
        v.setCreatedAt(now);
        v.setUpdatedAt(now);
        vendorRepository.save(v);
    }
}
