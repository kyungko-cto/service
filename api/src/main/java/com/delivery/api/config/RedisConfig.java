package com.delivery.api.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis 설정 - 대규모 트래픽 대응
 * 
 * 리팩토링 사항:
 * 1. Redis 직렬화 최적화: JSON 직렬화 사용
 * 2. 캐싱 활성화: @EnableCaching으로 캐시 기능 활성화
 * 3. 캐시 매니저 설정: TTL 및 직렬화 설정
 * 4. 성능 최적화: String 직렬화로 키 저장 공간 최적화
 * 
 * 메모리 누수 방지:
 * - RedisTemplate의 직렬화 설정으로 메모리 효율성 향상
 * - TTL 설정으로 오래된 데이터 자동 삭제
 */
@Configuration
@EnableCaching
public class RedisConfig {

    /**
     * RedisTemplate 설정 - 장바구니 등에 사용
     * 
     * 리팩토링: 직렬화 최적화
     * - String 키: StringRedisSerializer 사용 (메모리 효율)
     * - Object 값: GenericJackson2JsonRedisSerializer 사용 (유연성)
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // 키 직렬화: String 사용 (메모리 효율)
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        
        // 값 직렬화: JSON 사용 (유연성)
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        
        // 기본 직렬화 설정
        template.setDefaultSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        
        return template;
    }

    /**
     * 캐시 매니저 설정 - @Cacheable 어노테이션 사용
     * 
     * 리팩토링: 캐싱 전략 최적화
     * - TTL: 1시간 (기본값)
     * - 직렬화: JSON 사용
     * - 메모리 누수 방지: TTL로 자동 삭제
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))  // 기본 TTL: 1시간
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();  // null 값 캐싱 방지 (메모리 누수 방지)
        
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .transactionAware()  // 트랜잭션 지원
                .build();
    }
}
