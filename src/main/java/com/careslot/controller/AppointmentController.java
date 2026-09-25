package com.careslot.controller;

import com.careslot.db.generated.enums.UserRole;
import com.careslot.dto.appointment.AppointmentBookingRequest;
import com.careslot.dto.appointment.AppointmentResponse;
import com.careslot.dto.auth.ApiResponse;
import com.careslot.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<AppointmentResponse> bookAppointment(
            @Valid @RequestBody AppointmentBookingRequest request,
            Authentication authentication) {

        UUID patientUserId = UUID.fromString(authentication.getPrincipal().toString());
        AppointmentResponse response = appointmentService.bookAppointment(patientUserId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('PATIENT') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> cancelAppointment(
            @PathVariable UUID id,
            Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getPrincipal().toString());
        UserRole role = (UserRole) authentication.getAuthorities().stream()
                .findFirst().map(a -> UserRole.valueOf(a.getAuthority().replace("ROLE_", ""))).orElse(null);

        appointmentService.cancelAppointment(id, userId, role);
        return ResponseEntity.ok(new ApiResponse("Appointment cancelled"));
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasRole('CLINICIAN')")
    public ResponseEntity<ApiResponse> completeAppointment(
            @PathVariable UUID id,
            Authentication authentication) {

        UUID clinicianUserId = UUID.fromString(authentication.getPrincipal().toString());
        appointmentService.completeAppointment(id, clinicianUserId);
        return ResponseEntity.ok(new ApiResponse("Appointment completed"));
    }

    @GetMapping("/my-history")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<AppointmentResponse>> getMyHistory(Authentication authentication) {
        UUID patientUserId = UUID.fromString(authentication.getPrincipal().toString());
        return ResponseEntity.ok(appointmentService.getPatientHistory(patientUserId));
    }

    @GetMapping("/my-schedule")
    @PreAuthorize("hasRole('CLINICIAN')")
    public ResponseEntity<List<AppointmentResponse>> getMySchedule(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication authentication) {

        UUID clinicianUserId = UUID.fromString(authentication.getPrincipal().toString());
        return ResponseEntity.ok(appointmentService.getClinicianDailySchedule(clinicianUserId, date));
    }
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AppointmentResponse>> getAllAppointments() {

        return ResponseEntity.ok(appointmentService.getAllAppointment());
    }
}