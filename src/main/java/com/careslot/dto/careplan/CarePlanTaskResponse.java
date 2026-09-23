package com.careslot.dto.careplan;

import com.careslot.db.generated.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class CarePlanTaskResponse {
    private UUID id;
    private String title;
    private String description;
    private LocalDate dueDate;
    private Integer weight;
    private TaskStatus status;
}