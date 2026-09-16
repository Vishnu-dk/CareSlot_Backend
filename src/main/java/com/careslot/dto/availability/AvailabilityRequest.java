package com.careslot.dto.availability;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;

@Data
public class AvailabilityRequest {

    @NotNull(message = "Day of week is required")
    @Min(message ="Day must be btw 1 (Mon) and 7 (Sun) " , value = 1)
    @Max(message ="Day must be btw 1 (Mon) and 7 (Sun) " , value = 7)
    private Short dayOfWeek;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

}
