package com.hospital.hms.notification;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "hms.redis.enabled", havingValue = "true")
public class RedisNotificationInboxStore implements NotificationInboxStore {

    private static final String PREFIX = "hms:inbox:";

    private final StringRedisTemplate redis;
    private final int cap;

    public RedisNotificationInboxStore(
            StringRedisTemplate redis,
            @Value("${hms.notifications.inbox-cap-per-user:100}") int cap) {
        this.redis = redis;
        this.cap = cap;
    }

    @Override
    public void append(Long userId, String jsonEnvelope) {
        String key = PREFIX + userId;
        redis.opsForList().leftPush(key, jsonEnvelope);
        redis.opsForList().trim(key, 0, cap - 1);
    }

    @Override
    public List<String> listRecent(Long userId, int max) {
        List<String> range = redis.opsForList().range(PREFIX + userId, 0, max - 1L);
        return range != null ? range : List.of();
    }

    @Override
    public long size(Long userId) {
        Long n = redis.opsForList().size(PREFIX + userId);
        return n == null ? 0 : n;
    }
}
