package com.hospital.hms.domain;

import com.hospital.hms.domain.enums.PharmacyFulfillmentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "pharmacy_line_fulfillments")
@SQLRestriction("deleted_at IS NULL")
public class PharmacyLineFulfillment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_line_id", nullable = false, unique = true)
    private PrescriptionLine prescriptionLine;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PharmacyFulfillmentStatus status = PharmacyFulfillmentStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id")
    private Bill bill;

    @Column(name = "dispensed_at")
    private Instant dispensedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacist_user_id")
    private User pharmacist;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public Long getId() {
        return id;
    }

    public PrescriptionLine getPrescriptionLine() {
        return prescriptionLine;
    }

    public void setPrescriptionLine(PrescriptionLine prescriptionLine) {
        this.prescriptionLine = prescriptionLine;
    }

    public PharmacyFulfillmentStatus getStatus() {
        return status;
    }

    public void setStatus(PharmacyFulfillmentStatus status) {
        this.status = status;
    }

    public Bill getBill() {
        return bill;
    }

    public void setBill(Bill bill) {
        this.bill = bill;
    }

    public Instant getDispensedAt() {
        return dispensedAt;
    }

    public void setDispensedAt(Instant dispensedAt) {
        this.dispensedAt = dispensedAt;
    }

    public User getPharmacist() {
        return pharmacist;
    }

    public void setPharmacist(User pharmacist) {
        this.pharmacist = pharmacist;
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
