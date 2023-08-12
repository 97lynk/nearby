package io.a97lynk.nearby.websocket.controller;

import io.a97lynk.nearby.websocket.dto.Location;
import io.a97lynk.nearby.websocket.dto.Message;
import io.a97lynk.nearby.websocket.service.LocationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
@AllArgsConstructor
@Slf4j
public class LocationController {

    private final LocationService locationService;

    @PostMapping("/location/init")
    public void initLocation(@RequestBody Location location) throws UnknownHostException {
        locationService.initSubscriber(location);
    }

    // Handles messages from /app/chat. (Note the Spring adds the /app prefix for us).
    @MessageMapping("/location/{userId}")
    // Sends the return value of this method to /topic/messages
    @SendTo("/topic/location-history")
    public Message send(@DestinationVariable String userId, @Payload Location location) throws Exception {
        String time = new SimpleDateFormat("HH:mm:ss").format(new Date());

        Message returnMessage = new Message(userId, userId, location.toString(), time);

        locationService.periodicallyUpdateLocation(location, location.getUserId());
        return returnMessage;
    }
}
