package io.a97lynk.nearby.pubsub;

import io.a97lynk.nearby.ObjectUtil;
import io.a97lynk.nearby.dto.Location;
import io.a97lynk.nearby.service.LocationService;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Slf4j
@EqualsAndHashCode(of = {"userId", "friendId"})
public class RedisMessageSubscriber implements MessageListener {

    private String userId;

    private String friendId;

    private final SimpMessagingTemplate simpMessagingTemplate;

    private final LocationService locationService;

    public RedisMessageSubscriber(String userId, String friendId, SimpMessagingTemplate simpMessagingTemplate, LocationService locationService) {
        this.userId = userId;
        this.friendId = friendId;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.locationService = locationService;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String friendId = new String(message.getChannel()).split("_")[1];
        log.info("{} Message received: {} {}", userId, message, friendId);
        locationService.handleUpdateLocation(userId, ObjectUtil.jsonToObject(new String(message.getBody()), Location.class));
    }
}
