package com.hospital.hms.pharmacylock;

import java.time.Duration;
import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "hms.redis.enabled", havingValue = "true")
public class RedisPharmacyOperationLock implements PharmacyOperationLock {

    private static final String PREFIX = "hms:lock:pharmacy:";
    private static final Duration TTL = Duration.ofSeconds(45);

    private final StringRedisTemplate redis;

    public RedisPharmacyOperationLock(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public <T> T withBillLock(Long billId, Supplier<T> action) {
        return run(PREFIX + "bill:" + billId, action);
    }

    @Override
    public <T> T withPrescriptionLock(Long prescriptionId, Supplier<T> action) {
        return run(PREFIX + "rx:" + prescriptionId, action);
    }

    private <T> T run(String key, Supplier<T> action) {
        String token = UUID.randomUUID().toString();
        Boolean ok = redis.opsForValue().setIfAbsent(key, token, TTL);
        if (Boolean.FALSE.equals(ok)) {
            throw new com.hospital.hms.web.error.ApiException(
                    HttpStatus.CONFLICT, "Another pharmacy operation holds this lock; retry shortly");
        }
        try {
            return action.get();
        } finally {
            String cur = redis.opsForValue().get(key);
            if (token.equals(cur)) {
                redis.delete(key);
            }
        }
    }
}
