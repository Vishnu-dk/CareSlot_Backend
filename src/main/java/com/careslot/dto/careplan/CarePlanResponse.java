package com.careslot.dto.careplan;

import com.careslot.db.generated.enums.CarePlanStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class CarePlanResponse {
    private UUID id;
    private UUID patientId;
    private UUID clinicianId;
    private String clinicianName;
    private String title;
    private String description;
    private CarePlanStatus status;
    private BigDecimal progressPercentage;
    private OffsetDateTime createdAt;
    private List<CarePlanTaskResponse> tasks;
}