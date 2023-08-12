package io.a97lynk.nearby.websocket.pubsub;

import io.a97lynk.nearby.websocket.dto.Location;
import io.a97lynk.nearby.websocket.service.LocationService;
import io.a97lynk.nearby.websocket.util.ObjectUtil;
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
        log.info("[{}] RedisPubSub << Message received: {} {} {}", userId, userId, message, friendId);
        locationService.handleFriendUpdateLocation(userId, ObjectUtil.jsonToObject(new String(message.getBody()), Location.class));
    }
}
