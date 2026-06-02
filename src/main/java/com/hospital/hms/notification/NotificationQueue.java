package com.hospital.hms.notification;

import java.util.Optional;

/** Producer enqueue / consumer dequeue for async notification processing. */
public interface NotificationQueue {

    void enqueueJson(String json);

    /** Non-blocking pop; empty if queue drained. */
    Optional<String> dequeueJson();
}
