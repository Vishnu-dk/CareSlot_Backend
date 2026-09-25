package com.careslot.controller;

import com.careslot.dto.appointment.PatientAppointmentResponse;
import com.careslot.dto.auth.ApiResponse;
import com.careslot.dto.availability.WeeklyAvailabilityResponse;
import com.careslot.dto.clinician.ClinicianProfileRequest;
import com.careslot.dto.clinician.ClinicianResponse;
import com.careslot.dto.patient.PatientResponse;
import com.careslot.exception.ResourceNotFoundException;
import com.careslot.service.ClinicianService;
import com.careslot.service.SchedulingService;
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
    private final SchedulingService schedulingService;

    public ClinicianController(ClinicianService clinicianService, SchedulingService schedulingService) {
        this.clinicianService = clinicianService;
        this.schedulingService = schedulingService;
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

    @GetMapping("/weekly-availability/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<WeeklyAvailabilityResponse>> getWeeklyAvailability(
            Authentication authentication) {
        try {
            UUID userId = UUID.fromString(authentication.getPrincipal().toString());

            List<WeeklyAvailabilityResponse> schedule = schedulingService.getWeeklyAvailability(userId);
            return ResponseEntity.ok(schedule);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/my-patients")
    @PreAuthorize("hasRole('CLINICIAN')")
    public ResponseEntity<List<PatientAppointmentResponse>> getMyPatients(
            Authentication authentication
    ) {
        UUID userId = UUID.fromString(authentication.getPrincipal().toString());

        List<PatientAppointmentResponse> patients = clinicianService.getMyPatients(userId);
        return ResponseEntity.ok(patients);
    }
}