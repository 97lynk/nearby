package io.a97lynk.nearby.controller;

import io.a97lynk.nearby.dto.Location;
import io.a97lynk.nearby.dto.Message;
import io.a97lynk.nearby.service.LocationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
@AllArgsConstructor
@Slf4j
public class LocationController {

    private final LocationService locationService;

    @PostMapping("/location/init")
    public void initLocation(@RequestBody Location location) {
        log.info("initLocation {}", location.getUserId());
        locationService.initSubscriber(location.getUserId());
    }

    // Handles messages from /app/chat. (Note the Spring adds the /app prefix for us).
    @MessageMapping("/location/{userId}")
    // Sends the return value of this method to /topic/messages
    @SendTo("/topic/location-history")
    public Message send(@DestinationVariable String userId, @Payload Location location) throws Exception {
        log.info("receive {}", location);
        String time = new SimpleDateFormat("HH:mm:ss").format(new Date());

        Message returnMessage = new Message(userId, userId, location.toString(), time);

        locationService.updateLocation(location, location.getUserId());
        return returnMessage;
    }
}
