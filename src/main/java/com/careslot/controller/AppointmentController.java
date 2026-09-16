package com.careslot.controller;

import com.careslot.dto.appointment.AppointmentBookingRequest;
import com.careslot.dto.appointment.AppointmentResponse;
import com.careslot.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
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
}