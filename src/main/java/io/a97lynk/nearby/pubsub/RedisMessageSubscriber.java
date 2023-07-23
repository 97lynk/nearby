package io.a97lynk.nearby.pubsub;

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

    public RedisMessageSubscriber(String userId, String friendId, SimpMessagingTemplate simpMessagingTemplate) {
        this.userId = userId;
        this.friendId = friendId;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String friendId = new String(message.getChannel()).split("_")[1];
        log.info("{} Message received: {} {}", userId, message, friendId);
        simpMessagingTemplate.convertAndSend("/topic/online-friends/" + userId, new String(message.getBody()));
    }
}
