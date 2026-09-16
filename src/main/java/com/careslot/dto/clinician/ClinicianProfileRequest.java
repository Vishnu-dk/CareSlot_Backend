package com.careslot.dto.clinician;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ClinicianProfileRequest {
    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Specialty is required")
    private String specialty;

    @NotBlank(message = "License number is required")
    private String licenseNumber;
}