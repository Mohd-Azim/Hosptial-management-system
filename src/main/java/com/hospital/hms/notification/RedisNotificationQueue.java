package com.hospital.hms.notification;

import java.util.Optional;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * List-backed queue: RPUSH / LPOP on a single key (FIFO).
 */
public class RedisNotificationQueue implements NotificationQueue {

    public static final String QUEUE_KEY = "hms:queue:notifications";

    private final StringRedisTemplate redis;

    public RedisNotificationQueue(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public void enqueueJson(String json) {
        redis.opsForList().rightPush(QUEUE_KEY, json);
    }

    @Override
    public Optional<String> dequeueJson() {
        String v = redis.opsForList().leftPop(QUEUE_KEY);
        return Optional.ofNullable(v);
    }
}
