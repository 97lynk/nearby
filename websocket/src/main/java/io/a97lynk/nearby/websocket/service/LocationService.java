package io.a97lynk.nearby.websocket.service;

import io.a97lynk.nearby.websocket.dto.Location;
import io.a97lynk.nearby.websocket.dto.cache.LocationCacheDto;
import io.a97lynk.nearby.websocket.entity.Account;
import io.a97lynk.nearby.websocket.entity.Friendship;
import io.a97lynk.nearby.websocket.pubsub.MessagePublisher;
import io.a97lynk.nearby.websocket.pubsub.RedisMessageSubscriber;
import io.a97lynk.nearby.websocket.repository.FriendshipRepository;
import io.a97lynk.nearby.websocket.repository.LocationCacheRepository;
import io.a97lynk.nearby.websocket.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LocationService {

    private static final long NEAR_BY_DISTANCE = 5000;

    private final String serverName;

    private final MessagePublisher messagePublisher;

    private final SimpMessagingTemplate simpMessagingTemplate;

    private final RedisMessageListenerContainer messageListenerContainer;

    private final LocationCacheRepository locationCacheRepository;

    private final FriendshipRepository relationshipRepository;

    public LocationService(@Value("${server.name}") String serverName, MessagePublisher messagePublisher, SimpMessagingTemplate simpMessagingTemplate, RedisMessageListenerContainer messageListenerContainer,
                           LocationCacheRepository locationCacheRepository, FriendshipRepository relationshipRepository) {
        this.serverName = serverName;
        this.messagePublisher = messagePublisher;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.messageListenerContainer = messageListenerContainer;
        this.locationCacheRepository = locationCacheRepository;
        this.relationshipRepository = relationshipRepository;
    }

    public void initSubscriber(Location myLocation) {
        log.info("[{}] WS << initSubscriber  {}", myLocation.getUserId(), myLocation);
        myLocation.setSource(serverName);

        // 1. update location to the cache
        locationCacheRepository.saveLocation(myLocation.getUserId(), LocationCacheDto.builder()
                .latitude(myLocation.getLatitude())
                .longitude(myLocation.getLongitude())
                .updatedDate(new Date())
                .build());

        // 3. load all friend from db
        List<Friendship> friendshipList = relationshipRepository.findAllByAccountIdIn(Collections.singleton(myLocation.getUserId()));
        List<String> listFriendIds = friendshipList
                .stream()
                .map(Friendship::getFriend)
                .map(Account::getId)
                .collect(Collectors.toList());


        // 4. batch get all friend's locations from cache
        List<Location> friendLocations = getLocationsFromCache(listFriendIds);


        // 5. for each location, compute distance => return ws
        friendLocations.forEach(friendLocation -> {
            double distance = calDistance(myLocation, friendLocation);
            friendLocation.setDistance(distance);
            if (distance <= NEAR_BY_DISTANCE) {
                friendLocation.setNearby(true);
                friendLocations.add(friendLocation);
            }
        });
        simpMessagingTemplate.convertAndSend("/topic/nearby-friends/" + myLocation.getUserId(), ObjectUtil.object2Json(friendLocations));
        simpMessagingTemplate.convertAndSend("/topic/me/" + myLocation.getUserId(), ObjectUtil.object2Json(Collections.singletonMap("serverHandle", serverName)));


        // 6. for each friend => subscribe
        listFriendIds.forEach(friendId -> {
            log.info("[{}] WS << {} subscribe {}", myLocation.getUserId(), myLocation.getUserId(), friendId);
            RedisMessageSubscriber messageSubscriber = new RedisMessageSubscriber(myLocation.getUserId(), friendId, simpMessagingTemplate, this);
            messageListenerContainer.addMessageListener(messageSubscriber, new ChannelTopic("USER_" + friendId));
        });
    }

    public void handleFriendUpdateLocation(String userId, Location friendLocation) {
        getLocationFromCache(userId).ifPresent(myLocation -> {
            double distance = calDistance(myLocation, friendLocation);
            friendLocation.setDistance(distance);
            friendLocation.setNearby(distance <= NEAR_BY_DISTANCE);
            log.info("[{}] RedisPubSub >> send to WS {}", userId, myLocation);
            simpMessagingTemplate.convertAndSend("/topic/nearby-friends/" + userId, ObjectUtil.object2Json(friendLocation));
        });
    }

    public void periodicallyUpdateLocation(Location newLocation, String userId) {
        newLocation.setSource(serverName);
        log.info("[{}] WS << updateLocation {} {}", userId, newLocation, userId);

        // TODO 3. store the location into location history db

        // 4. save the location to redis cache
        locationCacheRepository.saveLocation(userId, LocationCacheDto.builder()
                .latitude(newLocation.getLatitude())
                .longitude(newLocation.getLongitude())
                .updatedDate(new Date())
                .build());

        // 5. publish the location to chanel
        messagePublisher.publish(newLocation, "USER_" + userId);
    }

    private double calDistance(Location from, Location to) {
        double diffLong = from.getLongitude() - to.getLongitude();
        double diffLat = from.getLatitude() - to.getLatitude();
        return Math.round(Math.sqrt(diffLong * diffLong + diffLat * diffLat));
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
