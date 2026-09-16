package com.careslot.service;

import com.careslot.db.generated.tables.records.AppointmentsRecord;
import com.careslot.db.generated.tables.records.ClinicianAvailabilityRecord;
import com.careslot.dto.availability.AvailabilityRequest;
import com.careslot.dto.availability.AvailableSlotResponse;
import com.careslot.exception.ResourceNotFoundException;
import com.careslot.repository.AppointmentRepository;
import com.careslot.repository.ClinicianAvailabilityRepository;
import com.careslot.repository.ClinicianRepository;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class SchedulingService {

    private final ClinicianRepository clinicianRepository;
    private final ClinicianAvailabilityRepository clinicianAvailabilityRepository;
    private final AppointmentRepository appointmentRepository;

    public SchedulingService(ClinicianRepository clinicianRepository,
                             ClinicianAvailabilityRepository clinicianAvailabilityRepository,
                             AppointmentRepository appointmentRepository) {
        this.clinicianRepository = clinicianRepository;
        this.clinicianAvailabilityRepository = clinicianAvailabilityRepository;
        this.appointmentRepository = appointmentRepository;
    }

    public void saveAvailability(UUID userId, AvailabilityRequest request) {
        // userId IS the clinicianId. Just verify the profile exists.
        if (!clinicianRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Clinician profile not found for user: " + userId);
        }
        clinicianAvailabilityRepository.saveAvailability(userId, request.getDayOfWeek(), request.getStartTime(), request.getEndTime());
    }

    public List<AvailableSlotResponse> getAvailableSlot(UUID userId, LocalDate date) {
        // 1. Verify profile exists
        if (!clinicianRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Clinician profile not found for user: " + userId);
        }

        // 2. Fetch availability using userId directly
        int dayOfWeek = date.getDayOfWeek().getValue();
        List<ClinicianAvailabilityRecord> availability = clinicianAvailabilityRepository.findByClinicianId(userId);

        ClinicianAvailabilityRecord dayAvailability = availability.stream()
                .filter(a -> a.getDayOfWeek() == dayOfWeek)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Clinician not available on this day"));

        LocalTime start = dayAvailability.getStartTime();
        LocalTime end = dayAvailability.getEndTime();
        Duration duration = Duration.ofMinutes(30);

        // 3. Fetch appointments using userId directly
        List<AppointmentsRecord> existingAppointments = appointmentRepository.findByClinicianAndDate(userId, date);
        List<AvailableSlotResponse> availableSlots = new ArrayList<>();
        LocalTime currentSlotStart = start;

        while (!currentSlotStart.plus(duration).isAfter(end)) {
            LocalTime currentSlotEnd = currentSlotStart.plus(duration);
            LocalTime finalSlotStart = currentSlotStart;
            LocalTime finalSlotEnd = currentSlotEnd;

            boolean isBooked = existingAppointments.stream()
                    .anyMatch(app -> {
                        LocalTime appStart = app.getStartsAt().toLocalTime();
                        LocalTime appEnd = app.getEndsAt().toLocalTime();
                        return finalSlotStart.isBefore(appEnd) && finalSlotEnd.isAfter(appStart);
                    });

            if (!isBooked) {
                availableSlots.add(AvailableSlotResponse.builder()
                        .startTime(currentSlotStart)
                        .endTime(currentSlotEnd)
                        .build());
            }
            currentSlotStart = currentSlotEnd;
        }
        return availableSlots;
    }
}