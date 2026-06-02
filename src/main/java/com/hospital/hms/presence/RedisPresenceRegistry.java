package com.hospital.hms.presence;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "hms.redis.enabled", havingValue = "true")
public class RedisPresenceRegistry implements PresenceRegistry {

    private static final String PREFIX = "hms:presence:";
    private final StringRedisTemplate redis;
    private final Duration ttl;

    public RedisPresenceRegistry(
            StringRedisTemplate redis,
            @Value("${hms.presence.ttl-minutes:15}") int ttlMinutes) {
        this.redis = redis;
        this.ttl = Duration.ofMinutes(ttlMinutes);
    }

    @Override
    public void touch(Long userId) {
        redis.opsForValue().set(PREFIX + userId, "1", ttl);
    }

    @Override
    public boolean isPresent(Long userId) {
        Boolean ok = redis.hasKey(PREFIX + userId);
        return Boolean.TRUE.equals(ok);
    }
}
