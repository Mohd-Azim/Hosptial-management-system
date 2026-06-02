package com.hospital.hms.service;

import com.hospital.hms.auth.UserPrincipal;
import com.hospital.hms.domain.MiscExpense;
import com.hospital.hms.domain.User;
import com.hospital.hms.domain.Vendor;
import com.hospital.hms.domain.enums.RoleCode;
import com.hospital.hms.repo.MiscExpenseRepository;
import com.hospital.hms.repo.UserRepository;
import com.hospital.hms.repo.VendorRepository;
import com.hospital.hms.web.dto.MiscExpenseRequest;
import com.hospital.hms.web.error.ApiException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MiscExpenseService {

    private final MiscExpenseRepository miscExpenseRepository;
    private final UserRepository userRepository;
    private final VendorRepository vendorRepository;

    public MiscExpenseService(MiscExpenseRepository miscExpenseRepository,
            UserRepository userRepository,
            VendorRepository vendorRepository) {
        this.miscExpenseRepository = miscExpenseRepository;
        this.userRepository = userRepository;
        this.vendorRepository = vendorRepository;
    }

    private static void assertFinance(UserPrincipal actor) {
        if (!actor.hasAny(RoleCode.HR, RoleCode.ADMIN)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "HR or admin only");
        }
    }

    @Transactional
    public MiscExpense record(UserPrincipal actor, MiscExpenseRequest req) {
        assertFinance(actor);
        User recorder = userRepository.findById(actor.userId()).orElseThrow();
        MiscExpense e = new MiscExpense();
        e.setCategory(req.category());
        e.setDescription(req.description());
        e.setAmount(req.amount());
        e.setExpenseDate(req.expenseDate());
        e.setRecordedBy(recorder);
        if (req.vendorId() != null) {
            Vendor v = vendorRepository.findById(req.vendorId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Vendor not found"));
            e.setVendor(v);
        }
        e.setCreatedAt(Instant.now());
        e.setUpdatedAt(Instant.now());
        return miscExpenseRepository.save(e);
    }

    @Transactional(readOnly = true)
    public List<MiscExpense> list(UserPrincipal actor, LocalDate from, LocalDate to) {
        assertFinance(actor);
        LocalDate f = from != null ? from : LocalDate.now().minusMonths(3);
        LocalDate t = to != null ? to : LocalDate.now().plusDays(1);
        return miscExpenseRepository.findByExpenseDateBetweenOrderByExpenseDateDesc(f, t);
    }

    @Transactional
    public void softDelete(UserPrincipal actor, Long id) {
        assertFinance(actor);
        MiscExpense e = miscExpenseRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Expense not found"));
        e.setDeletedAt(Instant.now());
        e.setUpdatedAt(Instant.now());
        miscExpenseRepository.save(e);
    }
}
