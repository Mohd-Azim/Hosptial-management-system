package com.hospital.hms.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.hms.presence.PresenceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationDeliveryService {

    private static final Logger log = LoggerFactory.getLogger(NotificationDeliveryService.class);

    private final ObjectMapper mapper;
    private final NotificationInboxStore inbox;
    private final PresenceRegistry presence;

    public NotificationDeliveryService(
            ObjectMapper mapper,
            NotificationInboxStore inbox,
            PresenceRegistry presence) {
        this.mapper = mapper;
        this.inbox = inbox;
        this.presence = presence;
    }

    public void deliver(String json) {
        try {
            NotificationEnvelope envelope = mapper.readValue(json, NotificationEnvelope.class);
            inbox.append(envelope.targetUserId(), json);
            if (presence.isPresent(envelope.targetUserId())) {
                log.debug(
                        "Notification {} ({}) stored for online user {}",
                        envelope.id(),
                        envelope.type(),
                        envelope.targetUserId());
            }
        } catch (Exception e) {
            log.warn("Skipping malformed notification payload", e);
        }
    }
}
