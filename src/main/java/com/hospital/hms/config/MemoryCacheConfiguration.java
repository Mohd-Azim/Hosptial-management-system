package com.hospital.hms.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@ConditionalOnProperty(name = "hms.redis.enabled", havingValue = "false", matchIfMissing = true)
public class MemoryCacheConfiguration {

    @Bean
    @Primary
    public CacheManager memoryCacheManager() {
        return new ConcurrentMapCacheManager("userDisplayNames");
    }
}
