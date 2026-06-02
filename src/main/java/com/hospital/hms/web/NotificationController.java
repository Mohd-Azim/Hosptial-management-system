package com.hospital.hms.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.hms.notification.NotificationEnvelope;
import com.hospital.hms.notification.NotificationInboxStore;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationInboxStore inboxStore;
    private final ObjectMapper objectMapper;

    public NotificationController(NotificationInboxStore inboxStore, ObjectMapper objectMapper) {
        this.inboxStore = inboxStore;
        this.objectMapper = objectMapper;
    }

    /**
     * Recent notifications for the logged-in user (inbox cache — avoids polling DB).
     */
    @GetMapping
    public List<NotificationEnvelope> recent(HttpServletRequest request) throws IOException {
        var p = WebRequests.principal(request);
        List<String> raw = inboxStore.listRecent(p.userId(), 50);
        List<NotificationEnvelope> out = new ArrayList<>(raw.size());
        for (String json : raw) {
            out.add(objectMapper.readValue(json, NotificationEnvelope.class));
        }
        return out;
    }
}
