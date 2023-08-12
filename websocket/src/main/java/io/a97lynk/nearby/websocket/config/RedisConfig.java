package io.a97lynk.nearby.websocket.config;

import io.a97lynk.nearby.websocket.dto.cache.LocationCacheDto;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.concurrent.Executors;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, LocationCacheDto> redisTemplate(RedisConnectionFactory jedisConnectionFactory) {
        RedisTemplate<String, LocationCacheDto> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(LocationCacheDto.class));
        return template;
    }
//
//    @Bean
//    public Topic topic() {
//        return new PatternTopic("USER_*");
//    }
//
//
//    @Bean
//    MessageListenerAdapter messageListener(RedisMessageSubscriber redisMessageSubscriber) {
//        return new MessageListenerAdapter(redisMessageSubscriber);
//    }

    @Bean
    RedisMessageListenerContainer redisContainer(RedisConnectionFactory redisConnectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
//        container.addMessageListener(messageListener(redisMessageSubscriber), topic());
        container.setSubscriptionExecutor(Executors.newFixedThreadPool(10));
        return container;
    }
}
