package com.hospital.hms.notification;

import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

public class InMemoryNotificationQueue implements NotificationQueue {

    private final ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();

    @Override
    public void enqueueJson(String json) {
        queue.add(json);
    }

    @Override
    public Optional<String> dequeueJson() {
        return Optional.ofNullable(queue.poll());
    }
}
