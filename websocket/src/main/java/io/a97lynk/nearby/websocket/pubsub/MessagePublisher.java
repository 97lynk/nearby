package io.a97lynk.nearby.websocket.pubsub;

public interface MessagePublisher {

    void publish(Object message, String topic);
}