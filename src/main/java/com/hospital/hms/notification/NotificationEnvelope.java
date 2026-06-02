package com.hospital.hms.notification;

import java.time.Instant;

/**
 * Serializable notification payload placed on the queue by producers and processed by the consumer.
 */
public record NotificationEnvelope(
        String id,
        NotificationType type,
        Long targetUserId,
        String title,
        String body,
        String referenceType,
        Long referenceId,
        Instant createdAt
) {

    public static NotificationEnvelope create(
            NotificationType type,
            Long targetUserId,
            String title,
            String body,
            String referenceType,
            Long referenceId) {
        return new NotificationEnvelope(
                java.util.UUID.randomUUID().toString(),
                type,
                targetUserId,
                title,
                body,
                referenceType,
                referenceId,
                Instant.now());
    }
}
