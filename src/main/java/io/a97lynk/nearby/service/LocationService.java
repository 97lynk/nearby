package io.a97lynk.nearby.service;

import io.a97lynk.nearby.ObjectUtil;
import io.a97lynk.nearby.dto.Location;
import io.a97lynk.nearby.entity.Account;
import io.a97lynk.nearby.entity.Relationship;
import io.a97lynk.nearby.pubsub.MessagePublisher;
import io.a97lynk.nearby.pubsub.RedisMessageSubscriber;
import io.a97lynk.nearby.repository.AccountRepository;
import io.a97lynk.nearby.repository.RelationshipRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class LocationService {


    private final MessagePublisher messagePublisher;

    private final RedisTemplate<String, Object> redisTemplate;

    private final SimpMessagingTemplate simpMessagingTemplate;

    private final RedisMessageListenerContainer messageListenerContainer;

    private final AccountRepository accountRepository;

    private final RelationshipRepository relationshipRepository;

    public void initSubscriber(Location myLocation) {
        log.info("initSubscriber {}", myLocation);

        // subscribe all friends
        List<String> listFriendIds = relationshipRepository.findAllByAccountIdIn(Collections.singleton(myLocation.getUserId()))
                .stream()
                .map(Relationship::getFriend)
                .map(Account::getId)
                .collect(Collectors.toList());
        listFriendIds.forEach(friendId -> {
            log.info("Add {} subscribe {}", myLocation.getUserId(), friendId);
            RedisMessageSubscriber messageSubscriber = new RedisMessageSubscriber(myLocation.getUserId(), friendId, simpMessagingTemplate, this);
            messageListenerContainer.addMessageListener(messageSubscriber, new ChannelTopic("USER_" + friendId));
        });

        // calculate distance
        List<Location> friendLocations = new LinkedList<>();
        listFriendIds.forEach(friendId -> {
            getLocationFromCache(friendId).ifPresent(friendLocation -> {
                if (isNearby(myLocation, friendLocation)) {
                    friendLocation.setNearby(true);
                    friendLocations.add(friendLocation);
                }
            });
        });
        simpMessagingTemplate.convertAndSend("/topic/nearby-friends/" + myLocation.getUserId(), ObjectUtil.object2Json(friendLocations));
    }

    public void handleUpdateLocation(String userId, Location friendLocation) {
        getLocationFromCache(userId).ifPresent(myLocation -> {
            log.info("cache {}", myLocation);
            if (isNearby(myLocation, friendLocation)) {
                friendLocation.setNearby(true);
                simpMessagingTemplate.convertAndSend("/topic/nearby-friends/" + userId, ObjectUtil.object2Json(friendLocation));
            }
        });
    }

    public void updateLocation(Location newLocation, String userId) {
        log.info("updateLocation {} {}", newLocation, userId);
        Location location = newLocation;
        LocalDateTime lastUpdated = LocalDateTime.now();

        // store redis
        redisTemplate.opsForValue().set("USER_" + userId, ObjectUtil.object2Json(newLocation), 6, TimeUnit.SECONDS);

        // publish redis pub/sub
        messagePublisher.publish(newLocation, "USER_" + userId);
    }

    private boolean isNearby(Location from, Location to) {
        double diffLong = Math.abs(from.getLongitude() - to.getLongitude());
        double diffLat = Math.abs(from.getLatitude() - to.getLatitude());
        return diffLong < 1000 || diffLat < 1000;
    }

    private Optional<Location> getLocationFromCache(String userId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get("USER_" + userId))
                .map(Object::toString)
                .map(s -> ObjectUtil.jsonToObject(s, Location.class));

    }

}
