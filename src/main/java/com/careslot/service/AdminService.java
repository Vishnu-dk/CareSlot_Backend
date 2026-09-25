package com.careslot.service;

import com.careslot.db.generated.tables.records.UsersRecord;
import com.careslot.exception.ResourceNotFoundException;
import com.careslot.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class AdminService {

    private final UserRepository userRepository;

    public AdminService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UsersRecord> getAllActiveUsers() {
        return userRepository.findAllActive();
    }

    public void deactivateUser(UUID userId) {
        UsersRecord user = userRepository.findByIdActive(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found or already deactivated"));

        userRepository.deactivateUser(userId);
    }

    public void activateUser(UUID userId) {
        UsersRecord user = userRepository.findAllUserById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not existed"));
        if (user.getDeletedAt() == null) {
            return;
        }

        userRepository.activateUser(userId);
    }
}