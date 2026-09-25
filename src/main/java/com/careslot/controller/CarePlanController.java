package com.careslot.controller;


import com.careslot.db.generated.enums.TaskStatus;
import com.careslot.dto.auth.ApiResponse;
import com.careslot.dto.careplan.CarePlanRequest;
import com.careslot.dto.careplan.CarePlanResponse;
import com.careslot.service.CarePlanService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/care-plan")
public class CarePlanController {
    private final CarePlanService carePlanService;


    public CarePlanController(CarePlanService carePlanService) {
        this.carePlanService = carePlanService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CLINICIAN')")
    public ResponseEntity<CarePlanResponse> createCarePlan(@Valid @RequestBody CarePlanRequest request,
                                                           Authentication authentication){
        UUID clinicianId=UUID.fromString(authentication.getPrincipal().toString());
        CarePlanResponse response=carePlanService.createCarePlan(clinicianId,request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my-plans")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<CarePlanResponse>> getMyCarePlans(Authentication authentication) {
        UUID patientUserId = UUID.fromString(authentication.getPrincipal().toString());
        return ResponseEntity.ok(carePlanService.getMyCarePlans(patientUserId));
    }

    @PatchMapping("/tasks/{taskId}/status")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse> updateTaskStatus(
            @PathVariable UUID taskId,
            @RequestParam TaskStatus status,
            Authentication authentication) {

        UUID patientUserId = UUID.fromString(authentication.getPrincipal().toString());
        carePlanService.updateTaskStatus(taskId, status, patientUserId);

        return ResponseEntity.ok(new ApiResponse("Assigned task updated"));
    }

    @GetMapping("/my-issued-plans")
    @PreAuthorize("hasRole('CLINICIAN')")
    public ResponseEntity<List<CarePlanResponse>> getIssuedPlans(Authentication authentication) {
        UUID clinicianId = UUID.fromString(authentication.getPrincipal().toString());
        return ResponseEntity.ok(carePlanService.getPlansByClinician(clinicianId));
    }


}
