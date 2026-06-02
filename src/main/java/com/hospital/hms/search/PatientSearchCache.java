package com.hospital.hms.search;

import java.util.Optional;

/** Redis-backed or no-op cache for patient prefix search (short TTL). */
public interface PatientSearchCache {

    Optional<String> getJson(String key);

    void putJson(String key, String json, long ttlSeconds);
}
