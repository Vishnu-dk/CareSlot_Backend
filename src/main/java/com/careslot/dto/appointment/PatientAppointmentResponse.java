package com.careslot.dto.appointment;

import com.careslot.db.generated.enums.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class PatientAppointmentResponse {
    private UUID userId;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String phoneNumber;
    private AppointmentStatus status;
    private LocalDate lastVisit;
}

