package com.hospital.hms.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "prescriptions")
@SQLRestriction("deleted_at IS NULL")
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visit_id", nullable = false, unique = true)
    private Visit visit;

    @Column(length = 1024)
    private String instructions;

    @Column(name = "printed_at")
    private Instant printedAt;

    @Column(name = "pharmacy_opt_in", nullable = false)
    private boolean pharmacyOptIn;

    @Column(name = "pharmacy_opt_in_at")
    private Instant pharmacyOptInAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public Long getId() {
        return id;
    }

    public Visit getVisit() {
        return visit;
    }

    public void setVisit(Visit visit) {
        this.visit = visit;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public Instant getPrintedAt() {
        return printedAt;
    }

    public void setPrintedAt(Instant printedAt) {
        this.printedAt = printedAt;
    }

    public boolean isPharmacyOptIn() {
        return pharmacyOptIn;
    }

    public void setPharmacyOptIn(boolean pharmacyOptIn) {
        this.pharmacyOptIn = pharmacyOptIn;
    }

    public Instant getPharmacyOptInAt() {
        return pharmacyOptInAt;
    }

    public void setPharmacyOptInAt(Instant pharmacyOptInAt) {
        this.pharmacyOptInAt = pharmacyOptInAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
