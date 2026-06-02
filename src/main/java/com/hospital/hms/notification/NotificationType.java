package com.hospital.hms.notification;

public enum NotificationType {
    /** Patient checked in; notify assigned doctor */
    PATIENT_ASSIGNED_TO_DOCTOR,
    /** Patient notified they are checked in */
    APPOINTMENT_CHECKIN_PATIENT,
    /** Prescription issued / visit completed */
    PRESCRIPTION_READY,
    /** Pharmacy bill paid */
    PHARMACY_PAYMENT_CONFIRMED,
    /** Medicines dispensed */
    PHARMACY_DISPENSED
}
