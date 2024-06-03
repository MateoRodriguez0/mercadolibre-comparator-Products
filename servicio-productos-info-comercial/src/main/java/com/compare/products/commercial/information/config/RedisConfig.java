package com.compare.products.commercial.information.config;

import java.time.Duration;
import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;

@Configuration
public class RedisConfig {

	@Bean
	RedisCacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
		RedisCacheManager cacheManager = RedisCacheManager.builder(connectionFactory)
			    .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig())
			    .transactionAware()
			    .withInitialCacheConfigurations(Collections.singletonMap("ProductInfoComercialCache",
			        RedisCacheConfiguration.defaultCacheConfig()
			        .entryTtl(Duration.ofMinutes(20))
			        .serializeValuesWith(SerializationPair.fromSerializer(
			        		new GenericJackson2JsonRedisSerializer()))))
			    .build();
				
		return cacheManager;
	}
	
}
