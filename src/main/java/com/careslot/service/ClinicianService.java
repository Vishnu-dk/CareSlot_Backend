package com.careslot.service;

import com.careslot.db.generated.tables.records.CliniciansRecord;
import com.careslot.dto.clinician.ClinicianProfileRequest;
import com.careslot.dto.clinician.ClinicianResponse;
import com.careslot.exception.ResourceNotFoundException;
import com.careslot.exception.UserAlreadyExistsException;
import com.careslot.repository.ClinicianRepository;
import com.careslot.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ClinicianService {

    private final ClinicianRepository clinicianRepo;
    private final UserRepository userRepo;

    public ClinicianService(ClinicianRepository clinicianRepo, UserRepository userRepo) {
        this.clinicianRepo = clinicianRepo;
        this.userRepo = userRepo;
    }

    public void createProfile(UUID userId, ClinicianProfileRequest request) {
        if (userRepo.findById(userId).isEmpty()) {
            throw new ResourceNotFoundException("User not found");
        }
        if (clinicianRepo.existsById(userId)) {
            throw new UserAlreadyExistsException("Clinician profile already exists for this user");
        }
        Optional<CliniciansRecord> record= clinicianRepo.findById(userId);

        boolean isSameLicense = clinicianRepo.findById(userId)
                .map(CliniciansRecord::getLicenseNumber)
                .filter(license -> license.equalsIgnoreCase(request.getLicenseNumber()))
                .isPresent();

        if(isSameLicense){
            throw new UserAlreadyExistsException("Licence number exists");
        }

        clinicianRepo.saveProfile(userId, request.getFirstName(), request.getLastName(), request.getSpecialty(), request.getLicenseNumber());
    }

    public List<ClinicianResponse> getAllClinicians() {
        return clinicianRepo.findAll().stream()
                .map(c -> ClinicianResponse.builder()
                        .userId(c.getId())
                        .firstName(c.getFirstName())
                        .lastName(c.getLastName())
                        .specialty(c.getSpecialty())
                        .build())
                .collect(Collectors.toList());
    }
}