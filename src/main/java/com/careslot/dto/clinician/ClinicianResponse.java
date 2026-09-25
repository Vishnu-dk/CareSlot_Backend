package com.careslot.dto.clinician;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class ClinicianResponse {
    private UUID userId;
    private String firstName;
    private String lastName;
    private String specialty;
    private String email;
    private LocalDate deletedAt;
}