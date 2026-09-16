package com.careslot.controller;

import com.careslot.dto.auth.ApiResponse;
import com.careslot.dto.patient.PatientProfileRequest;
import com.careslot.dto.patient.PatientResponse;
import com.careslot.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @PostMapping("/profile")
    @PreAuthorize("hasRole('PATIENT') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> createProfile(
            @Valid @RequestBody PatientProfileRequest request,
            Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getPrincipal().toString());
        patientService.createProfile(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse("Patient profile created successfully"));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PatientResponse>> getAllPatients() {
        List<PatientResponse> patients = patientService.getAllPatients();
        return ResponseEntity.ok(patients);
    }

    @GetMapping("/my-profile")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<PatientResponse> getMyProfile(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getPrincipal().toString());
        PatientResponse profile = patientService.getProfileByUserId(userId);
        return ResponseEntity.ok(profile);
    }
}