package com.hospital.hms.config;

import java.time.Duration;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Pooled Lettuce connection factory when Redis is enabled ({@code hms.redis.enabled=true}).
 * {@link org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration} is excluded so startup works
 * without Redis unless explicitly enabled.
 */
@Configuration
@ConditionalOnProperty(name = "hms.redis.enabled", havingValue = "true")
@EnableConfigurationProperties(RedisProperties.class)
public class RedisStackConfiguration {

    @Bean
    public LettuceConnectionFactory redisConnectionFactory(RedisProperties props) {
        RedisStandaloneConfiguration standalone = new RedisStandaloneConfiguration();
        standalone.setHostName(props.getHost());
        standalone.setPort(props.getPort());
        if (props.getUsername() != null) {
            standalone.setUsername(props.getUsername());
        }
        if (props.getPassword() != null) {
            standalone.setPassword(RedisPassword.of(props.getPassword()));
        }
        standalone.setDatabase(props.getDatabase());

        RedisProperties.Lettuce lettuce = props.getLettuce();
        RedisProperties.Pool pool = lettuce.getPool();
        GenericObjectPoolConfig<Object> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(pool.getMaxActive() > 0 ? pool.getMaxActive() : 16);
        poolConfig.setMaxIdle(pool.getMaxIdle() > 0 ? pool.getMaxIdle() : 8);
        poolConfig.setMinIdle(pool.getMinIdle() > 0 ? pool.getMinIdle() : 2);
        if (pool.getMaxWait() != null) {
            poolConfig.setMaxWait(pool.getMaxWait());
        }
        poolConfig.setTestOnBorrow(true);

        Duration commandTimeout = props.getTimeout() != null ? props.getTimeout() : Duration.ofSeconds(2);
        Duration shutdownTimeout = lettuce.getShutdownTimeout() != null
                ? lettuce.getShutdownTimeout()
                : Duration.ofMillis(100);

        LettucePoolingClientConfiguration clientConfiguration = LettucePoolingClientConfiguration.builder()
                .poolConfig(poolConfig)
                .commandTimeout(commandTimeout)
                .shutdownTimeout(shutdownTimeout)
                .build();

        return new LettuceConnectionFactory(standalone, clientConfiguration);
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        StringRedisTemplate t = new StringRedisTemplate();
        t.setConnectionFactory(redisConnectionFactory);
        t.afterPropertiesSet();
        return t;
    }

    @Bean
    @Primary
    public CacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration cfg = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(15))
                .disableCachingNullValues();
        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(cfg)
                .build();
    }
}
