package com.hospital.hms.pharmacylock;

import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "hms.redis.enabled", havingValue = "false", matchIfMissing = true)
public class LocalPharmacyOperationLock implements PharmacyOperationLock {

    private static final int STRIPES = 256;
    private final ReentrantLock[] stripes;

    public LocalPharmacyOperationLock() {
        this.stripes = Stream.generate(ReentrantLock::new).limit(STRIPES).toArray(ReentrantLock[]::new);
    }

    private ReentrantLock stripe(long id) {
        int i = (int) ((id ^ (id >>> 32)) & (STRIPES - 1));
        return stripes[i];
    }

    @Override
    public <T> T withBillLock(Long billId, Supplier<T> action) {
        return runStriped(billId, action);
    }

    @Override
    public <T> T withPrescriptionLock(Long prescriptionId, Supplier<T> action) {
        return runStriped(prescriptionId, action);
    }

    private <T> T runStriped(Long id, Supplier<T> action) {
        ReentrantLock lock = stripe(id);
        lock.lock();
        try {
            return action.get();
        } finally {
            lock.unlock();
        }
    }
}
