package io.a97lynk.nearby.service;

import io.a97lynk.nearby.ObjectUtil;
import io.a97lynk.nearby.dto.Location;
import io.a97lynk.nearby.pubsub.MessagePublisher;
import io.a97lynk.nearby.pubsub.RedisMessageSubscriber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class LocationService {

    private Map<String, List<String>> FRIENDS = new ConcurrentHashMap<>();

    private final MessagePublisher messagePublisher;

    private final RedisTemplate<String, Object> redisTemplate;

    private final SimpMessagingTemplate simpMessagingTemplate;

    private final RedisMessageListenerContainer messageListenerContainer;

    public LocationService(MessagePublisher messagePublisher, RedisTemplate<String, Object> redisTemplate,
                           SimpMessagingTemplate simpMessagingTemplate, RedisMessageListenerContainer messageListenerContainer) {
        this.messagePublisher = messagePublisher;
        this.redisTemplate = redisTemplate;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.messageListenerContainer = messageListenerContainer;
        FRIENDS.put("tony", Arrays.asList("tuan"));
        FRIENDS.put("tuan", Arrays.asList("tony", "duyen"));
        FRIENDS.put("duyen", Arrays.asList("tuan"));
    }

    public void initSubscriber(String userId) {
        FRIENDS.getOrDefault(userId, Collections.emptyList())
                .forEach(f -> {
                    log.info("Add {} subscribe {}", userId, f);
                    RedisMessageSubscriber messageSubscriber = new RedisMessageSubscriber(userId, f, simpMessagingTemplate);
                    messageListenerContainer.addMessageListener(messageSubscriber, new ChannelTopic("USER_" + f));
                });
    }

    public void updateLocation(Location newLocation, String userId) {
        log.info("updateLocation {} {}", newLocation, userId);
        Location location = newLocation;
        LocalDateTime lastUpdated = LocalDateTime.now();

        redisTemplate.opsForValue().set("USER_" + userId, ObjectUtil.object2Json(newLocation), 6, TimeUnit.SECONDS);
        messagePublisher.publish(newLocation, "USER_" + userId);
    }

}
