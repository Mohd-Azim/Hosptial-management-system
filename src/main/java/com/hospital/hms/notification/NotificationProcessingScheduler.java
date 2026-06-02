package com.hospital.hms.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NotificationProcessingScheduler {

    private final NotificationQueue queue;
    private final NotificationDeliveryService delivery;
    private final int batchSize;

    public NotificationProcessingScheduler(
            NotificationQueue queue,
            NotificationDeliveryService delivery,
            @Value("${hms.notifications.batch-size:32}") int batchSize) {
        this.queue = queue;
        this.delivery = delivery;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedDelayString = "${hms.notifications.poll-ms:400}", initialDelayString = "${hms.notifications.poll-ms:400}")
    public void drain() {
        for (int i = 0; i < batchSize; i++) {
            var next = queue.dequeueJson();
            if (next.isEmpty()) {
                break;
            }
            delivery.deliver(next.get());
        }
    }
}
