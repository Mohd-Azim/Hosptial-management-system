package com.hospital.hms.presence;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "hms.redis.enabled", havingValue = "false", matchIfMissing = true)
public class InMemoryPresenceRegistry implements PresenceRegistry {

    private final ConcurrentHashMap<Long, Long> lastSeenMillis = new ConcurrentHashMap<>();
    private final long ttlMillis;

    public InMemoryPresenceRegistry(@Value("${hms.presence.ttl-minutes:15}") int ttlMinutes) {
        this.ttlMillis = Duration.ofMinutes(ttlMinutes).toMillis();
    }

    @Override
    public void touch(Long userId) {
        lastSeenMillis.put(userId, System.currentTimeMillis());
    }

    @Override
    public boolean isPresent(Long userId) {
        Long t = lastSeenMillis.get(userId);
        return t != null && System.currentTimeMillis() - t <= ttlMillis;
    }
}
