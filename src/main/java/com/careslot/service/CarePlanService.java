package com.careslot.service;


import com.careslot.db.generated.enums.TaskStatus;
import com.careslot.db.generated.tables.records.CarePlanTasksRecord;
import com.careslot.db.generated.tables.records.CarePlansRecord;
import com.careslot.dto.careplan.CarePlanRequest;
import com.careslot.dto.careplan.CarePlanResponse;
import com.careslot.exception.ResourceNotFoundException;
import com.careslot.exception.UserAlreadyExistsException;
import com.careslot.repository.AppointmentRepository;
import com.careslot.repository.CarePlanRepository;
import com.careslot.repository.CarePlanTaskRepository;
import com.careslot.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CarePlanService {

    private final PatientRepository patientRepository;
    private final CarePlanRepository carePlanRepository;
    private final CarePlanTaskRepository carePlanTaskRepository;
    private final AppointmentRepository appointmentRepository;


    public CarePlanService(PatientRepository patientRepository, CarePlanRepository carePlanRepository, CarePlanTaskRepository carePlanTaskRepository, AppointmentRepository appointmentRepository) {
        this.patientRepository = patientRepository;
        this.carePlanRepository = carePlanRepository;
        this.carePlanTaskRepository = carePlanTaskRepository;
        this.appointmentRepository = appointmentRepository;
    }

    public CarePlanResponse createCarePlan(UUID clinicianUserId, CarePlanRequest request) {
        if (patientRepository.findByUserId(request.getPatientId()).isEmpty()) {
            throw new ResourceNotFoundException("Patient profile not found.");
        }

        if (!appointmentRepository.existsValidAppointment(request.getPatientId(), clinicianUserId)) {
            throw new ResourceNotFoundException("Cannot create care plan: No 'BOOKED' appointment found between this clinician and patient.");
        }

        if (carePlanRepository.existsActivePlan(request.getPatientId(), clinicianUserId)) {
            throw new UserAlreadyExistsException("An active care plan has already been issued to this patient.");
        }

        CarePlansRecord planRecord = carePlanRepository.save(request.getPatientId(), clinicianUserId, request.getTitle(), request.getDescription());
        request.getTasks().forEach(task ->
                carePlanTaskRepository.save(planRecord.getId(), task.getTitle(), task.getDescription(), task.getDueDate(), task.getWeight())
        );

        return mapToResponse(planRecord);
    }

    public List<CarePlanResponse> getMyCarePlans(UUID patientUserId) {

        boolean hasBookedAppointment=appointmentRepository.hasBookedAppointment(patientUserId);

        if (!hasBookedAppointment) {
            throw new ResourceNotFoundException("You do not have any ongoing treatments or booked appointments with a clinician.");
        }

        List<CarePlansRecord> plans = carePlanRepository.findByPatientId(patientUserId);
        if (plans.isEmpty()) {
            throw new ResourceNotFoundException("You have booked appointments, but no care plan has been issued by your clinician yet.");
        }

        return plans.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public void updateTaskStatus(UUID taskId, TaskStatus newStatus, UUID patientUserId) {
        CarePlanTasksRecord task = carePlanTaskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        CarePlansRecord plan = carePlanRepository.findById(task.getCarePlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Care plan not found"));

        if (!plan.getPatientId().equals(patientUserId)) {
            throw new ResourceNotFoundException("You do not have permission to update this task");
        }

        carePlanTaskRepository.updateTaskStatus(taskId, newStatus);
        BigDecimal newProgress = calculateProgress(plan.getId());
        carePlanRepository.updateProgress(plan.getId(), newProgress);
    }

    public BigDecimal calculateProgress(UUID planId){
        List<CarePlanTasksRecord> tasks = carePlanTaskRepository.findByPlanId(planId);

        if (tasks.isEmpty()) return BigDecimal.ZERO;

        int totalWeight=tasks.stream()
                .mapToInt(t->t.getWeight()!=null?t.getWeight():1)
                .sum();

        int completedWeight=tasks.stream()
                .filter(t->t.getStatus()==TaskStatus.COMPLETED)
                .mapToInt(t->t.getWeight()!=null?t.getWeight():1)
                .sum();

        if(totalWeight==0) return BigDecimal.ZERO;

        return BigDecimal.valueOf(completedWeight)
                .divide(BigDecimal.valueOf(totalWeight),2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

    }

    private CarePlanResponse mapToResponse(CarePlansRecord plansRecord) {

        return CarePlanResponse.builder()
                .id(plansRecord.getId())
                .title(plansRecord.getTitle())
                .description(plansRecord.getDescription())
                .clinicianId(plansRecord.getClinicianId())
                .patientId(plansRecord.getPatientId())
                .status(plansRecord.getStatus())
                .progressPercentage(plansRecord.getProgressPercentage())
                .createdAt(plansRecord.getCreatedAt())
                .build();
    }
}
