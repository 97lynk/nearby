package io.a97lynk.nearby.service;

import io.a97lynk.nearby.ObjectUtil;
import io.a97lynk.nearby.dto.Location;
import io.a97lynk.nearby.dto.cache.LocationCacheDto;
import io.a97lynk.nearby.entity.Account;
import io.a97lynk.nearby.entity.Friendship;
import io.a97lynk.nearby.pubsub.MessagePublisher;
import io.a97lynk.nearby.pubsub.RedisMessageSubscriber;
import io.a97lynk.nearby.repository.AccountRepository;
import io.a97lynk.nearby.repository.FriendshipRepository;
import io.a97lynk.nearby.repository.LocationCacheRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LocationService {

    private final String serverName;

    private final MessagePublisher messagePublisher;

    private final RedisTemplate<String, Object> redisTemplate;

    private final SimpMessagingTemplate simpMessagingTemplate;

    private final RedisMessageListenerContainer messageListenerContainer;

    private final AccountRepository accountRepository;

    private final LocationCacheRepository locationCacheRepository;

    private final FriendshipRepository relationshipRepository;

    public LocationService(@Value("${server.name}") String serverName, MessagePublisher messagePublisher, RedisTemplate<String, Object> redisTemplate, SimpMessagingTemplate simpMessagingTemplate, RedisMessageListenerContainer messageListenerContainer,
                           AccountRepository accountRepository, LocationCacheRepository locationCacheRepository, FriendshipRepository relationshipRepository) {
        this.serverName = serverName;
        this.messagePublisher = messagePublisher;
        this.redisTemplate = redisTemplate;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.messageListenerContainer = messageListenerContainer;
        this.accountRepository = accountRepository;
        this.locationCacheRepository = locationCacheRepository;
        this.relationshipRepository = relationshipRepository;
    }

    public void initSubscriber(Location myLocation) {
        log.info("[{}] WS << initSubscriber  {}", myLocation.getUserId(), myLocation);
        myLocation.setSource(serverName);

        // subscribe all friends
        List<String> listFriendIds = relationshipRepository.findAllByAccountIdIn(Collections.singleton(myLocation.getUserId()))
                .stream()
                .map(Friendship::getFriend)
                .map(Account::getId)
                .collect(Collectors.toList());

        listFriendIds.forEach(friendId -> {
            log.info("[{}] WS << {} subscribe {}", myLocation.getUserId(), myLocation.getUserId(), friendId);
            RedisMessageSubscriber messageSubscriber = new RedisMessageSubscriber(myLocation.getUserId(), friendId, simpMessagingTemplate, this);
            messageListenerContainer.addMessageListener(messageSubscriber, new ChannelTopic("USER_" + friendId));
        });

        // calculate distance
        List<Location> friendLocations =  getLocationsFromCache(listFriendIds);
        friendLocations.forEach(friendLocation -> {
            if (isNearby(myLocation, friendLocation)) {
                friendLocation.setNearby(true);
                friendLocations.add(friendLocation);
            }
        });

        simpMessagingTemplate.convertAndSend("/topic/nearby-friends/" + myLocation.getUserId(), ObjectUtil.object2Json(friendLocations));
        simpMessagingTemplate.convertAndSend("/topic/me/" + myLocation.getUserId(), ObjectUtil.object2Json(Collections.singletonMap("serverHandle", serverName)));
    }

    public void handleFriendUpdateLocation(String userId, Location friendLocation) {
        getLocationFromCache(userId).ifPresent(myLocation -> {
            friendLocation.setNearby(isNearby(myLocation, friendLocation));
            log.info("[{}] RedisPubSub >> send to WS {}", userId, myLocation);
            simpMessagingTemplate.convertAndSend("/topic/nearby-friends/" + userId, ObjectUtil.object2Json(friendLocation));
        });
    }

    public void periodicallyUpdateLocation(Location newLocation, String userId) throws UnknownHostException {
        newLocation.setSource(serverName);
        log.info("[{}] WS << updateLocation {} {}", userId, newLocation, userId);

        // store redis
        locationCacheRepository.saveLocation(userId, LocationCacheDto.builder()
                .latitude(newLocation.getLatitude())
                .longitude(newLocation.getLongitude())
                .updatedDate(new Date())
                .build());

        // publish redis pub/sub
        messagePublisher.publish(newLocation, "USER_" + userId);
    }

    private boolean isNearby(Location from, Location to) {
        double diffLong = Math.abs(from.getLongitude() - to.getLongitude());
        double diffLat = Math.abs(from.getLatitude() - to.getLatitude());
        return diffLong < 1000 || diffLat < 1000;
    }

    private List<Location> getLocationsFromCache(List<String> userIds) {
        return locationCacheRepository.getLocations(userIds)
                .stream()
                .filter(Objects::nonNull)
                .map(l -> Location.builder()
                        .latitude(l.getLatitude())
                        .longitude(l.getLongitude())
                        .userId(l.getUserId())
                        .build())
                .collect(Collectors.toList());

    }

    private Optional<Location> getLocationFromCache(String userId) {
        return Optional.ofNullable(locationCacheRepository.getLocation(userId))
                .map(l -> Location.builder()
                        .latitude(l.getLatitude())
                        .longitude(l.getLongitude())
                        .userId(l.getUserId())
                        .build());
    }

}
