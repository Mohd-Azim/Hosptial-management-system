package com.hospital.hms.config;

import com.hospital.hms.notification.InMemoryNotificationQueue;
import com.hospital.hms.notification.NotificationQueue;
import com.hospital.hms.notification.RedisNotificationQueue;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class NotificationInfrastructureConfig {

    @Bean
    @ConditionalOnProperty(name = "hms.redis.enabled", havingValue = "true")
    public NotificationQueue redisNotificationQueue(StringRedisTemplate stringRedisTemplate) {
        return new RedisNotificationQueue(stringRedisTemplate);
    }

    @Bean
    @ConditionalOnProperty(name = "hms.redis.enabled", havingValue = "false", matchIfMissing = true)
    public NotificationQueue inMemoryNotificationQueue() {
        return new InMemoryNotificationQueue();
    }
}
