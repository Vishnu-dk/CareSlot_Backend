package com.careslot.service;

import com.careslot.db.generated.enums.AppointmentStatus;
import com.careslot.db.generated.enums.UserRole;
import com.careslot.db.generated.tables.records.AppointmentsRecord;
import com.careslot.db.generated.tables.records.PatientsRecord;
import com.careslot.dto.appointment.AppointmentBookingRequest;
import com.careslot.dto.appointment.AppointmentResponse;
import com.careslot.exception.ResourceNotFoundException;
import com.careslot.exception.SlotNotAvailableException;
import com.careslot.repository.AppointmentRepository;
import com.careslot.repository.ClinicianRepository;
import com.careslot.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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


        if (appointmentRepository.existsOverlappingForPatient(patient.getId(), startsAt, endsAt)) {
            throw new SlotNotAvailableException("You already have an appointment scheduled at this time.");
        }
        if (appointmentRepository.existsValidAppointment(patient.getId(), request.getClinicianId())) {
            throw new SlotNotAvailableException("You already have an appointment with this clinician");
        }

        AppointmentsRecord record;

        Optional<AppointmentsRecord> cancelledAppointment = appointmentRepository
                .findCancelledAppointment(request.getClinicianId(), startsAt, endsAt);

        if (cancelledAppointment.isPresent()) {
            record = cancelledAppointment.get();
            record.setPatientId(patient.getId());
            record.setReasonForVisit(request.getReason());
            record.setStatus(AppointmentStatus.BOOKED);
            record.store();
        } else {
            try {
                record = appointmentRepository.save(patient.getId(), request.getClinicianId(), startsAt, endsAt, request.getReason());
            } catch (RuntimeException e) {
                if (e.getMessage().equals("Slot is already booked")) {
                    throw new SlotNotAvailableException("This time slot has just been booked by someone else. Please choose another time.");
                }
                throw e;
            }
        }

        return AppointmentResponse.builder()
                .id(record.getId())
                .patientId(record.getPatientId())
                .clinicianId(record.getClinicianId())
                .startsAt(record.getStartsAt())
                .endsAt(record.getEndsAt())
                .status(record.getStatus())
                .reason(record.getReasonForVisit())
                .build();

    }

    public void cancelAppointment(UUID appointmentId, UUID userId, UserRole role){
        AppointmentsRecord appointment=appointmentRepository.findById(appointmentId)
                .orElseThrow(()->new ResourceNotFoundException("Appointment not found"));

        if(role!=UserRole.ADMIN && !appointment.getPatientId().equals(userId)){
            throw new IllegalArgumentException("You dont have permission to cancel this appointment");
        }


        if(appointment.getStatus()== AppointmentStatus.CANCELLED){
            throw new IllegalArgumentException("Appointment already cancelled");
        }
        if(appointment.getStatus()==AppointmentStatus.COMPLETED){
            throw new IllegalArgumentException("Cannot cancel a completed appointment");
        }
        if (OffsetDateTime.now().plusHours(24).isAfter(appointment.getStartsAt())) {
            throw new IllegalStateException("Appointments can only be cancelled at least 24 hours in advance");
        }
        appointmentRepository.updateStatus(appointmentId,AppointmentStatus.CANCELLED);
    }

    public void completeAppointment(UUID appointmentId,UUID clinicianId){
        AppointmentsRecord appointment=appointmentRepository.findById(appointmentId)
                .orElseThrow(()->new ResourceNotFoundException("Appointment not found"));
        if(!appointment.getClinicianId().equals(clinicianId)){
            throw new IllegalArgumentException("Only assigned Clinician for this appointment");
        }

        if(appointment.getStatus()!=AppointmentStatus.BOOKED){
            throw new IllegalArgumentException("Only Booked appointment can be completed");
        }
        appointmentRepository.updateStatus(appointmentId,AppointmentStatus.COMPLETED);
    }

    public List<AppointmentResponse> getPatientHistory(UUID patientId){
        return appointmentRepository.findByPatientId(patientId).stream()
                .map(this::mapResponse)
                .collect(Collectors.toList());
    }

    public List<AppointmentResponse> getAllAppointment(){
        return appointmentRepository.findAllAppointment().stream()
                .map(this::mapResponse)
                .collect(Collectors.toList());
    }

    public List<AppointmentResponse> getClinicianDailySchedule(UUID clinicianId, LocalDate date){
        return appointmentRepository.findClinicianSchedule(clinicianId,date).stream()
                .map(this::mapResponse)
                .collect(Collectors.toList());
    }

    private AppointmentResponse mapResponse(AppointmentsRecord record) {

        String clinicianName = clinicianRepository.findById(record.getClinicianId())
                .map(c -> "Dr. " + c.getFirstName() + " " + c.getLastName()).orElse("Unknown");
        String patientName = patientRepository.findByUserId(record.getPatientId())
                .map(p -> p.getFirstName() + " " + p.getLastName()).orElse("Unknown");
        return AppointmentResponse.builder()
                .id(record.getId())
                .patientId(record.getPatientId())
                .patientName(patientName)
                .clinicianId(record.getClinicianId())
                .clinicianName(clinicianName)
                .startsAt(record.getStartsAt())
                .endsAt(record.getEndsAt())
                .status(record.getStatus())
                .reason(record.getReasonForVisit())
                .build();
    }
}