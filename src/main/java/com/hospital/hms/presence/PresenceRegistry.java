package com.hospital.hms.presence;

public interface PresenceRegistry {

    /** Refresh “online” TTL for portal/API activity. */
    void touch(Long userId);

    /** Whether the user had recent activity (session refresh window). */
    boolean isPresent(Long userId);
}
