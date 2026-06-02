package com.hospital.hms.pharmacylock;

import java.util.function.Supplier;

/** Serialize pharmacy billing / dispensing against the same prescription or bill (Redis or JVM stripes). */
public interface PharmacyOperationLock {

    <T> T withBillLock(Long billId, Supplier<T> action);

    <T> T withPrescriptionLock(Long prescriptionId, Supplier<T> action);
}
