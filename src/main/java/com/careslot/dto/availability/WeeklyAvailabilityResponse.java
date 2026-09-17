package com.careslot.dto.availability;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
public class WeeklyAvailabilityResponse {
    private Short dayOfWeek;
    private String dayName;
    private LocalTime startTime;
    private LocalTime endTime;
}