package com.hospital.hms.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Locale;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "users")
@SQLRestriction("deleted_at IS NULL")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 128)
    private String username;

    @Column(nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    /** Uppercase normalized full name for prefix search (indexed with {@link #nameBucket}). */
    @Column(name = "sort_name_norm", nullable = false, length = 255)
    private String sortNameNorm = "";

    /** First letter A–Z of {@link #sortNameNorm}, or '#' for digits/symbols — supports bucket pruning. */
    @Column(name = "name_bucket", nullable = false, length = 1)
    private String nameBucket = "#";

    @Column(length = 32)
    private String phone;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private boolean suspended;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
        refreshSortKeys();
    }

    public String getSortNameNorm() {
        return sortNameNorm;
    }

    public void setSortNameNorm(String sortNameNorm) {
        this.sortNameNorm = sortNameNorm;
    }

    public String getNameBucket() {
        return nameBucket;
    }

    public void setNameBucket(String nameBucket) {
        this.nameBucket = nameBucket;
    }

    @PrePersist
    @PreUpdate
    void refreshSortKeysOnPersist() {
        refreshSortKeys();
    }

    private void refreshSortKeys() {
        if (fullName == null || fullName.isBlank()) {
            sortNameNorm = "";
            nameBucket = "#";
            return;
        }
        String collapsed = fullName.trim().replaceAll("\\s+", " ");
        String upper = collapsed.toUpperCase(Locale.ROOT);
        sortNameNorm = upper;
        char c = upper.charAt(0);
        nameBucket = (c >= 'A' && c <= 'Z') ? String.valueOf(c) : "#";
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isSuspended() {
        return suspended;
    }

    public void setSuspended(boolean suspended) {
        this.suspended = suspended;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(Instant revokedAt) {
        this.revokedAt = revokedAt;
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
