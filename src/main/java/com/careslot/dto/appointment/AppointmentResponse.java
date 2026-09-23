package com.careslot.dto.appointment;

import com.careslot.db.generated.enums.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class AppointmentResponse {
    private UUID id;
    private UUID patientId;
    private String patientName;
    private UUID clinicianId;
    private String clinicianName;
    private OffsetDateTime startsAt;
    private OffsetDateTime endsAt;
    private AppointmentStatus status;
    private String reason;
}