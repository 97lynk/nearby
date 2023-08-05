package io.a97lynk.nearby.pubsub;

import io.a97lynk.nearby.dto.Location;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.Topic;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class RedisMessagePublisher implements MessagePublisher {

    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void publish(Object message, String topic) {
        log.info("[{}] WS >> Message sent: {} to {}", ((Location) message).getUserId(), message, topic);
        redisTemplate.convertAndSend(topic, message);
    }
}
