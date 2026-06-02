package com.hospital.hms.notification;

import java.util.List;

public interface NotificationInboxStore {

    void append(Long userId, String jsonEnvelope);

    List<String> listRecent(Long userId, int max);

    long size(Long userId);
}
