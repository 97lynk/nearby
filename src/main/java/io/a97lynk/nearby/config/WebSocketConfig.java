package io.a97lynk.nearby.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.ByteArrayMessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration;
import org.springframework.web.socket.config.annotation.WebMvcStompWebSocketEndpointRegistration;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
//         Set prefix for the endpoint that the client listens for our messages from
        registry.enableSimpleBroker("/topic");

//         Set prefix for endpoints the client will send messages to
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                // Allow the origin http://localhost:63343 to send messages to us. (Base url of the client)
                .setAllowedOrigins("http://localhost:63342")
                // Registers the endpoint where the handshake will take place
                .addInterceptors(new HttpHandshakeInterceptor());
        registry.addEndpoint("/ws").setAllowedOrigins("http://localhost:63342").addInterceptors(new HttpHandshakeInterceptor()).withSockJS();
    }

    //    @Override
//    public void configureClientInboundChannel(ChannelRegistration registration) {
//
//        // Add our interceptor for authentication/authorization
//        registration.interceptors(channelInterceptor);
//
//    }


}