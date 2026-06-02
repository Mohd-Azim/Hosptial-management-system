package com.hospital.hms.search;

import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "hms.redis.enabled", havingValue = "false", matchIfMissing = true)
public class NoOpPatientSearchCache implements PatientSearchCache {

    @Override
    public Optional<String> getJson(String key) {
        return Optional.empty();
    }

    @Override
    public void putJson(String key, String json, long ttlSeconds) {
        // no-op
    }
}
