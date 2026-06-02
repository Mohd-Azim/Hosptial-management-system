package com.hospital.hms.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.hms.repo.PatientProfileRepository;
import com.hospital.hms.search.PatientSearchCache;
import com.hospital.hms.web.dto.PatientSearchHit;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PatientSearchService {

    private static final String CACHE_PREFIX = "hms:search:pat:v1:";

    private final PatientProfileRepository patientProfileRepository;
    private final PatientSearchCache patientSearchCache;
    private final ObjectMapper objectMapper;
    private final int minChars;
    private final int defaultLimit;
    private final long cacheTtlSeconds;

    public PatientSearchService(
            PatientProfileRepository patientProfileRepository,
            PatientSearchCache patientSearchCache,
            ObjectMapper objectMapper,
            @Value("${hms.search.patient.min-chars:2}") int minChars,
            @Value("${hms.search.patient.limit:24}") int defaultLimit,
            @Value("${hms.search.patient.cache-ttl-seconds:45}") long cacheTtlSeconds) {
        this.patientProfileRepository = patientProfileRepository;
        this.patientSearchCache = patientSearchCache;
        this.objectMapper = objectMapper;
        this.minChars = minChars;
        this.defaultLimit = defaultLimit;
        this.cacheTtlSeconds = cacheTtlSeconds;
    }

    @Transactional(readOnly = true)
    public List<PatientSearchHit> searchPatients(String rawQuery, Integer limit) {
        String norm = normalize(rawQuery);
        if (norm.length() < minChars) {
            return List.of();
        }
        int lim = limit != null && limit > 0 && limit <= 100 ? limit : defaultLimit;

        String cacheKey = CACHE_PREFIX + sha256Hex(norm);
        try {
            var cached = patientSearchCache.getJson(cacheKey);
            if (cached.isPresent()) {
                return objectMapper.readValue(cached.get(), new TypeReference<List<PatientSearchHit>>() {});
            }
        } catch (Exception ignored) {
            // fall through to DB
        }

        String bucket = bucketFor(norm);
        List<PatientSearchHit> hits = patientProfileRepository.searchByBucketAndPrefix(
                bucket, norm, PageRequest.of(0, lim));

        try {
            patientSearchCache.putJson(cacheKey, objectMapper.writeValueAsString(hits), cacheTtlSeconds);
        } catch (Exception ignored) {
            // caching optional
        }
        return hits;
    }

    private static String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
    }

    private static String bucketFor(String norm) {
        if (norm.isEmpty()) {
            return "#";
        }
        char c = norm.charAt(0);
        return (c >= 'A' && c <= 'Z') ? String.valueOf(c) : "#";
    }

    private static String sha256Hex(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] dig = md.digest(s.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(dig);
        } catch (Exception e) {
            return Integer.toHexString(s.hashCode());
        }
    }
}
