package com.hospital.hms.domain;

import com.hospital.hms.domain.enums.AppointmentStatus;
import com.hospital.hms.domain.enums.BookingChannel;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "appointments")
@SQLRestriction("deleted_at IS NULL")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_user_id", nullable = false)
    private User patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booked_by_user_id", nullable = false)
    private User bookedBy;

    @Column(name = "scheduled_at", nullable = false)
    private Instant scheduledAt;

    /** IST calendar date for indexed daily partitions / clinical queues (derived from {@link #scheduledAt}). */
    @Column(name = "scheduled_local_date")
    private LocalDate scheduledLocalDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "booking_channel", nullable = false, length = 32)
    private BookingChannel bookingChannel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AppointmentStatus status = AppointmentStatus.PENDING_PAYMENT;

    @Column(name = "consultation_fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal consultationFee = BigDecimal.ZERO;

    @Column(length = 512)
    private String notes;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public Long getId() {
        return id;
    }

    public User getPatient() {
        return patient;
    }

    public void setPatient(User patient) {
        this.patient = patient;
    }

    public User getBookedBy() {
        return bookedBy;
    }

    public void setBookedBy(User bookedBy) {
        this.bookedBy = bookedBy;
    }

    public Instant getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(Instant scheduledAt) {
        this.scheduledAt = scheduledAt;
        syncScheduledLocalDate();
    }

    public LocalDate getScheduledLocalDate() {
        return scheduledLocalDate;
    }

    public void setScheduledLocalDate(LocalDate scheduledLocalDate) {
        this.scheduledLocalDate = scheduledLocalDate;
    }

    @PrePersist
    @PreUpdate
    void syncScheduledLocalDate() {
        if (scheduledAt != null) {
            scheduledLocalDate = scheduledAt.atZone(ZoneId.of("Asia/Kolkata")).toLocalDate();
        }
    }

    public BookingChannel getBookingChannel() {
        return bookingChannel;
    }

    public void setBookingChannel(BookingChannel bookingChannel) {
        this.bookingChannel = bookingChannel;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public BigDecimal getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(BigDecimal consultationFee) {
        this.consultationFee = consultationFee;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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
