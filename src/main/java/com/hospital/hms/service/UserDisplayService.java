package com.hospital.hms.service;

import com.hospital.hms.domain.User;
import com.hospital.hms.repo.UserRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class UserDisplayService {

    private final UserRepository userRepository;

    public UserDisplayService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Cacheable(cacheNames = "userDisplayNames", key = "#userId", unless = "#userId == null")
    public String fullName(Long userId) {
        return userRepository.findById(userId).map(User::getFullName).orElse("User");
    }
}
