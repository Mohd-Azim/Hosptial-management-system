package com.hospital.hms.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class NotificationProducer {

    private final ObjectMapper mapper;
    private final NotificationQueue queue;

    public NotificationProducer(ObjectMapper mapper, NotificationQueue queue) {
        this.mapper = mapper;
        this.queue = queue;
    }

    public void publish(NotificationEnvelope envelope) {
        try {
            queue.enqueueJson(mapper.writeValueAsString(envelope));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to enqueue notification", e);
        }
    }
}
