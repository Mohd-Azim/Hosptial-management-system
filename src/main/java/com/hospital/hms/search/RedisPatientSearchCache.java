package com.hospital.hms.search;

import java.time.Duration;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "hms.redis.enabled", havingValue = "true")
public class RedisPatientSearchCache implements PatientSearchCache {

    private final StringRedisTemplate redis;

    public RedisPatientSearchCache(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public Optional<String> getJson(String key) {
        return Optional.ofNullable(redis.opsForValue().get(key));
    }

    @Override
    public void putJson(String key, String json, long ttlSeconds) {
        redis.opsForValue().set(key, json, Duration.ofSeconds(Math.max(1, ttlSeconds)));
    }
}
