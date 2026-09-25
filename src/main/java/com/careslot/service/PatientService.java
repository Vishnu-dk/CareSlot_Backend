package com.careslot.service;

import com.careslot.db.generated.tables.records.UsersRecord;
import com.careslot.dto.patient.PatientProfileRequest;
import com.careslot.dto.patient.PatientResponse;
import com.careslot.exception.ResourceNotFoundException;
import com.careslot.exception.UserAlreadyExistsException;
import com.careslot.repository.PatientRepository;
import com.careslot.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PatientService {

    private final PatientRepository patientRepo;
    private final UserRepository userRepo;

    public PatientService(PatientRepository patientRepo, UserRepository userRepo) {
        this.patientRepo = patientRepo;
        this.userRepo = userRepo;
    }

    public void createProfile(UUID userId, PatientProfileRequest request) {
        if (!userRepo.findById(userId).isPresent()) {
            throw new ResourceNotFoundException("User not found");
        }
        if (patientRepo.existsByUserId(userId)) {
            throw new UserAlreadyExistsException("Patient profile already exists for this user");
        }
        patientRepo.saveProfile(userId, request.getFirstName(), request.getLastName(),
                request.getDateOfBirth(), request.getPhoneNumber());
    }

    public List<PatientResponse> getAllPatients() {
        return patientRepo.findAll().stream()
                .map(p -> {
                    Optional<UsersRecord> userRecord = userRepo.findAllUserById(p.getId());
                    String email = null;
                    LocalDate deletedAt=null;

                    if (userRecord.isPresent()) {
                        email = userRecord.get().getEmail();
                        deletedAt = Optional.ofNullable(userRecord.get().getDeletedAt())
                                .map(OffsetDateTime::toLocalDate)
                                .orElse(null);

                    }

                    return PatientResponse.builder()
                            .userId(p.getUserId())
                            .firstName(p.getFirstName())
                            .lastName(p.getLastName())
                            .dateOfBirth(p.getDateOfBirth())
                            .phoneNumber(p.getPhoneNumber())
                            .email(email)
                            .deletedAt(deletedAt)
                            .build();
                })
                .collect(Collectors.toList());
    }


    public PatientResponse getProfileByUserId(UUID userId) {
        return patientRepo.findByUserId(userId)
                .map(p -> PatientResponse.builder()
                        .userId(p.getUserId())
                        .firstName(p.getFirstName())
                        .lastName(p.getLastName())
                        .dateOfBirth(p.getDateOfBirth())
                        .phoneNumber(p.getPhoneNumber())
                        .build())
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found"));
    }
}