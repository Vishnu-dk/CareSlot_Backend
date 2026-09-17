package com.careslot.dto.careplan;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class CarePlanRequest {
    @NotNull(message = "Patient ID is required")
    private UUID patientId;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotEmpty(message = "At least one task is required")
    @Valid
    private List<CarePlanTaskRequest> tasks;
}