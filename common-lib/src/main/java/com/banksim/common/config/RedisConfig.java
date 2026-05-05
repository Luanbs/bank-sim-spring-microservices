package com.banksim.common.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@ConditionalOnClass(RedisConnectionFactory.class)
public class RedisConfig {

    @Configuration
    @ConditionalOnClass(ReactiveRedisConnectionFactory.class)
    static class ReactiveRedisConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "reactiveRedisTemplate")
        @ConditionalOnBean(ReactiveRedisConnectionFactory.class)
        public ReactiveRedisTemplate<String, String> reactiveRedisTemplate(ReactiveRedisConnectionFactory factory) {
            StringRedisSerializer serializer = new StringRedisSerializer();
            RedisSerializationContext<String, String> context = RedisSerializationContext
                    .<String, String>newSerializationContext(serializer)
                    .value(serializer)
                    .build();
            return new ReactiveRedisTemplate<>(factory, context);
        }
    }

    @Configuration
    static class ServletRedisConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "redisTemplate")
        @ConditionalOnBean(RedisConnectionFactory.class)
        public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory factory) {
            RedisTemplate<String, String> template = new RedisTemplate<>();
            template.setConnectionFactory(factory);
            template.setKeySerializer(new StringRedisSerializer());
            template.setValueSerializer(new StringRedisSerializer());
            return template;
        }
    }
}