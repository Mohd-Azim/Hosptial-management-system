package com.hospital.hms.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "medical_waste_logs")
@SQLRestriction("deleted_at IS NULL")
public class MedicalWasteLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category_code", nullable = false, length = 64)
    private String categoryCode;

    @Column(length = 512)
    private String description;

    @Column(name = "quantity_value", nullable = false, precision = 12, scale = 4)
    private BigDecimal quantityValue;

    @Column(name = "quantity_unit", nullable = false, length = 32)
    private String quantityUnit;

    @Column(name = "segregation_status", nullable = false, length = 64)
    private String segregationStatus;

    @Column(name = "storage_location")
    private String storageLocation;

    @Column(name = "handed_over_to_authorised_agent", nullable = false)
    private boolean handedOverToAuthorisedAgent;

    @Column(name = "cbwtf_manifest_number", length = 128)
    private String cbwtfManifestNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by_user_id", nullable = false)
    private User recordedBy;

    @Column(name = "incident_notes", length = 1024)
    private String incidentNotes;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt = Instant.now();

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public Long getId() {
        return id;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getQuantityValue() {
        return quantityValue;
    }

    public void setQuantityValue(BigDecimal quantityValue) {
        this.quantityValue = quantityValue;
    }

    public String getQuantityUnit() {
        return quantityUnit;
    }

    public void setQuantityUnit(String quantityUnit) {
        this.quantityUnit = quantityUnit;
    }

    public String getSegregationStatus() {
        return segregationStatus;
    }

    public void setSegregationStatus(String segregationStatus) {
        this.segregationStatus = segregationStatus;
    }

    public String getStorageLocation() {
        return storageLocation;
    }

    public void setStorageLocation(String storageLocation) {
        this.storageLocation = storageLocation;
    }

    public boolean isHandedOverToAuthorisedAgent() {
        return handedOverToAuthorisedAgent;
    }

    public void setHandedOverToAuthorisedAgent(boolean handedOverToAuthorisedAgent) {
        this.handedOverToAuthorisedAgent = handedOverToAuthorisedAgent;
    }

    public String getCbwtfManifestNumber() {
        return cbwtfManifestNumber;
    }

    public void setCbwtfManifestNumber(String cbwtfManifestNumber) {
        this.cbwtfManifestNumber = cbwtfManifestNumber;
    }

    public User getRecordedBy() {
        return recordedBy;
    }

    public void setRecordedBy(User recordedBy) {
        this.recordedBy = recordedBy;
    }

    public String getIncidentNotes() {
        return incidentNotes;
    }

    public void setIncidentNotes(String incidentNotes) {
        this.incidentNotes = incidentNotes;
    }

    public Instant getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(Instant recordedAt) {
        this.recordedAt = recordedAt;
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
}
