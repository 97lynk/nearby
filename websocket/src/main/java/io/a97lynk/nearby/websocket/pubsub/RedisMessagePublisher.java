package io.a97lynk.nearby.websocket.pubsub;

import io.a97lynk.nearby.websocket.dto.Location;
import io.a97lynk.nearby.websocket.dto.cache.LocationCacheDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class RedisMessagePublisher implements MessagePublisher {

    private RedisTemplate<String, LocationCacheDto> redisTemplate;

    @Override
    public void publish(Object message, String topic) {
        log.info("[{}] WS >> Message sent: {} to {}", ((Location) message).getUserId(), message, topic);
        redisTemplate.convertAndSend(topic, message);
    }
}
