package com.careslot.controller;

import com.careslot.dto.auth.ApiResponse;
import com.careslot.dto.availability.AvailableSlotResponse;
import com.careslot.dto.availability.AvailabilityRequest;
import com.careslot.dto.availability.WeeklyAvailabilityResponse;
import com.careslot.service.SchedulingService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/scheduling")
public class SchedulingController {

    private final SchedulingService schedulingService;

    public SchedulingController(SchedulingService schedulingService) {
        this.schedulingService = schedulingService;
    }

    @PostMapping("/availability")
    @PreAuthorize("hasRole('CLINICIAN') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> setAvailability(
            @Valid @RequestBody AvailabilityRequest request,
            Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getPrincipal().toString());
        schedulingService.saveAvailability(userId, request);
        return ResponseEntity.ok(new ApiResponse("Availability added"));
    }
    @PutMapping("/availability")
    @PreAuthorize("hasRole('CLINICIAN') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> updateAvailability(
            @Valid @RequestBody AvailabilityRequest request,
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getPrincipal().toString());
        schedulingService.updateAvailability(userId, request);
        return ResponseEntity.ok(new ApiResponse("Availability updated"));
    }

    @GetMapping("/clinicians/{userId}/slots")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AvailableSlotResponse>> getClinicianAvailableSlots(
            @PathVariable UUID userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<AvailableSlotResponse> slots = schedulingService.getAvailableSlot(userId, date);
        return ResponseEntity.ok(slots);
    }
    @GetMapping("/clinicians/{userId}/weekly-availability")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<WeeklyAvailabilityResponse>> getWeeklyAvailability(
            @PathVariable UUID userId) {
        List<WeeklyAvailabilityResponse> weeklySchedule = schedulingService.getWeeklyAvailability(userId);
        return ResponseEntity.ok(weeklySchedule);
    }
}