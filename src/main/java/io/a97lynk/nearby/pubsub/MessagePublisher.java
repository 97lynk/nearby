package io.a97lynk.nearby.pubsub;

public interface MessagePublisher {

    void publish(Object message, String topic);
}