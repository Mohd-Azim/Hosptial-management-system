package com.hospital.hms.notification;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "hms.redis.enabled", havingValue = "false", matchIfMissing = true)
public class InMemoryNotificationInboxStore implements NotificationInboxStore {

    private static final int CAP = 100;
    private final ConcurrentHashMap<Long, Deque<String>> rows = new ConcurrentHashMap<>();

    @Override
    public void append(Long userId, String jsonEnvelope) {
        rows.compute(userId, (k, q) -> {
            Deque<String> d = q != null ? q : new ArrayDeque<>();
            d.addFirst(jsonEnvelope);
            while (d.size() > CAP) {
                d.removeLast();
            }
            return d;
        });
    }

    @Override
    public List<String> listRecent(Long userId, int max) {
        Deque<String> d = rows.get(userId);
        if (d == null || d.isEmpty()) {
            return List.of();
        }
        int n = Math.min(max, d.size());
        List<String> out = new ArrayList<>(n);
        int i = 0;
        for (String s : d) {
            if (i++ >= n) {
                break;
            }
            out.add(s);
        }
        return Collections.unmodifiableList(out);
    }

    @Override
    public long size(Long userId) {
        Deque<String> d = rows.get(userId);
        return d == null ? 0 : d.size();
    }
}
