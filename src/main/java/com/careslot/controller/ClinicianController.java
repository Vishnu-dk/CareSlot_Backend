package com.careslot.controller;

import com.careslot.dto.auth.ApiResponse;
import com.careslot.dto.clinician.ClinicianProfileRequest;
import com.careslot.dto.clinician.ClinicianResponse;
import com.careslot.service.ClinicianService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/clinicians")
public class ClinicianController {

    private final ClinicianService clinicianService;

    public ClinicianController(ClinicianService clinicianService) {
        this.clinicianService = clinicianService;
    }

    @PostMapping("/profile")
    @PreAuthorize("hasRole('CLINICIAN') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> createProfile(
            @Valid @RequestBody ClinicianProfileRequest request,
            Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getPrincipal().toString());
        clinicianService.createProfile(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse("Profile Added"));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ClinicianResponse>> getAllClinicians() {
        List<ClinicianResponse> clinicians = clinicianService.getAllClinicians();
        return ResponseEntity.ok(clinicians);
    }
}