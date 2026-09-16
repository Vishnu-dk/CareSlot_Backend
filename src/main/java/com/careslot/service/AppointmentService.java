package com.careslot.service;

import com.careslot.db.generated.tables.records.PatientsRecord;
import com.careslot.dto.appointment.AppointmentBookingRequest;
import com.careslot.dto.appointment.AppointmentResponse;
import com.careslot.exception.ResourceNotFoundException;
import com.careslot.exception.SlotNotAvailableException;
import com.careslot.repository.AppointmentRepository;
import com.careslot.repository.ClinicianRepository;
import com.careslot.repository.PatientRepository;
import org.springframework.stereotype.Service;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final ClinicianRepository clinicianRepository;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              PatientRepository patientRepository,
                              ClinicianRepository clinicianRepository) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.clinicianRepository = clinicianRepository;
    }

    public AppointmentResponse bookAppointment(UUID patientUserId, AppointmentBookingRequest request) {
        PatientsRecord patient = patientRepository.findByUserId(patientUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found. Please complete your profile."));

        if (!clinicianRepository.existsById(request.getClinicianId())) {
            throw new ResourceNotFoundException("Clinician not found");
        }

        OffsetDateTime startsAt = OffsetDateTime.of(request.getDate(), request.getStartTime(), ZoneOffset.UTC);
        OffsetDateTime endsAt = OffsetDateTime.of(request.getDate(), request.getEndTime(), ZoneOffset.UTC);

        try {
            var record = appointmentRepository.save(patient.getId(), request.getClinicianId(), startsAt, endsAt, request.getReason());

            return AppointmentResponse.builder()
                    .id(record.getId())
                    .patientId(record.getPatientId())
                    .clinicianId(record.getClinicianId())
                    .startsAt(record.getStartsAt())
                    .endsAt(record.getEndsAt())
                    .status(record.getStatus())
                    .reason(record.getReasonForVisit())
                    .build();
        } catch (RuntimeException e) {
            if (e.getMessage().equals("Slot is already booked")) {
                throw new SlotNotAvailableException("This time slot has just been booked by someone else. Please choose another time.");
            }
            throw e;
        }
    }
}