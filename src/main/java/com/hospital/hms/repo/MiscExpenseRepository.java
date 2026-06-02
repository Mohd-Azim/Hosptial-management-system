package com.hospital.hms.repo;

import com.hospital.hms.domain.MiscExpense;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MiscExpenseRepository extends JpaRepository<MiscExpense, Long> {

    List<MiscExpense> findByExpenseDateBetweenOrderByExpenseDateDesc(LocalDate from, LocalDate to);
}
